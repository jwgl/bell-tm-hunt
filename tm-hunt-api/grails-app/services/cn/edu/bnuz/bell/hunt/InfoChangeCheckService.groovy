package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.gorm.transactions.Transactional

import javax.annotation.Resource

@Transactional
class InfoChangeCheckService {
    DataAccessService dataAccessService
    InfoChangeService infoChangeService
    @Resource(name = 'projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler

    private static getCounts(String userId) {
        def result = InfoChange.executeQuery '''
select new map(
    sum (case when form.status = 'SUBMITTED' then 1 else 0 end) as todo,
    sum (case when form.status in (:doneStates) then 1 else 0 end) as done
)
from InfoChange form
where form.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
)
''', [userId: userId, doneStates: [State.CHECKED, State.CLOSED]]
        [
                (ListType.TODO): result ? result[0].todo : 0,
                (ListType.DONE): result ? result[0].done : 0,
        ]
    }

    def list(String userId, ListCommand cmd) {
        switch (cmd.type) {
            case ListType.TODO:
                return findTodoList(userId, cmd.args)
            case ListType.DONE:
                return findDoneList(userId, cmd.args)
            default:
                throw new BadRequestException()
        }
    }

    def findTodoList(String userId, Map args) {
        def forms = InfoChange.executeQuery '''
select new map(
    form.id as id,
    project.code as code,
    project.name as name,
    project.level as level,
    project.subtype.name as subtype,
    form.dateSubmitted as date,
    form.type as type,
    form.status as status
)
from InfoChange form
join form.project project
where form.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) and form.status = :status
''', [userId: userId, status: State.SUBMITTED], args
        return [forms: forms, counts: getCounts(userId)]
    }

    def findDoneList(String userId, Map args) {
        def forms = InfoChange.executeQuery '''
select new map(
    form.id as id,
    project.code as code,
    project.name as name,
    project.level as level,
    project.subtype.name as subtype,
    form.dateChecked as date,
    form.type as type,
    form.status as status
)
from InfoChange form
join form.project project
where form.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) and form.status in (:doneStates)
''', [userId: userId, doneStates: [State.CHECKED, State.CLOSED]], args
        return [forms: forms, counts: getCounts(userId)]
    }

    def getFormForCheck(String userId, Long id, ListType type, String activity) {
        def form = infoChangeService.getInfoForShow(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${InfoChange.WORKFLOW_ID}.${activity}"),
                User.load(userId),
        )
        domainStateMachineHandler.checkReviewer(id, userId, activity)
        return [
                form      : form,
                project   : infoChangeService.findProject(form?.projectId),
                counts    : getCounts(userId),
                workitemId: workitem ? workitem.id : null,
                prevId    : getPrevCheckId(userId, id, type),
                nextId    : getNextCheckId(userId, id, type),
        ]
    }

    def getFormForCheck(String userId, Long id, ListType type, UUID workitemId) {
        def form = infoChangeService.getInfoForShow(id)

        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, userId, activity)

        return [
                form      : form,
                counts    : getCounts(userId),
                workitemId: workitemId,
                prevId    : getPrevCheckId(userId, id, type),
                nextId    : getNextCheckId(userId, id, type),
        ]
    }

    private Long getPrevCheckId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) and form.status = :status
and form.dateSubmitted < (select dateSubmitted from InfoChange where id = :id)
order by form.dateSubmitted asc
''', [userId: userId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) and form.dateChecked > (select dateChecked from InfoChange where id = :id)
order by form.dateChecked asc
''', [userId: userId, id: id])
        }
    }

    private Long getNextCheckId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) and form.status = :status
and form.dateSubmitted > (select dateSubmitted from InfoChange where id = :id)
order by form.dateSubmitted asc
''', [userId: userId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) and form.dateChecked < (select dateChecked from InfoChange where id = :id)
order by form.dateChecked desc
''', [userId: userId, id: id])
        }
    }

    void accept(String userId, AcceptCommand cmd, UUID workitemId) {
        InfoChange form = InfoChange.get(cmd.id)

        if (form.status != State.SUBMITTED) {
            return
        }

        domainStateMachineHandler.accept(form, userId, Activities.CHECK, cmd.comment, workitemId, cmd.to)
        form.checker = Teacher.load(userId)
        form.dateChecked = new Date()
        form.approver = null
        form.dateApproved = null
        form.save()
    }

    void reject(String userId, RejectCommand cmd, UUID workitemId) {
        InfoChange form = InfoChange.get(cmd.id)
        if (form.status != State.SUBMITTED) {
            return
        }
        domainStateMachineHandler.reject(form, userId, Activities.CHECK, cmd.comment, workitemId)
        form.checker = Teacher.load(userId)
        form.dateChecked = new Date()
        form.save()
    }
}
