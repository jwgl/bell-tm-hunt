package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
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

/**
 * 处长加签
 */
@Transactional
class InfoChangeReviewService {
    DataAccessService dataAccessService
    SecurityService securityService
    InfoChangeService infoChangeService
    @Resource(name = 'infoChangeReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler

    private static getCounts(String userId) {
        return [
                (ListType.TODO): InfoChange.countByReviewerAndDateReviewedIsNull(Teacher.load(userId)),
                (ListType.DONE): InfoChange.countByReviewerAndDateReviewedIsNotNull(Teacher.load(userId)),
        ]
    }

    def list(ListCommand cmd) {
        switch (cmd.type) {
            case ListType.TODO:
                return findTodoList(cmd)
            case ListType.DONE:
                return findDoneList(cmd)
            default:
                throw new BadRequestException()
        }
    }

    def findTodoList(ListCommand cmd) {
        def forms = InfoChange.executeQuery '''
select new map(
    form.id as id,
    project.code as code,
    project.name as name,
    project.level as level,
    project.subtype.name as subtype,
    form.dateChecked as date,
    form.type as type,
    form.dateReviewed as dateReviewed,
    form.status as status
)
from InfoChange form
join form.project project
where (form.status = :status or (form.status = 'SUBMITTED' and 1 = any_element(form.type)))
and form.dateReviewed is null
and form.reviewer.id = :userId
order by form.dateChecked
''', [status: State.CHECKED, userId: securityService.userId], cmd.args
        return [forms: forms, counts: getCounts(securityService.userId)]
    }

    def findDoneList(ListCommand cmd) {
        def forms = InfoChange.executeQuery '''
select new map(
    form.id as id,
    project.code as code,
    project.name as name,
    project.level as level,
    project.subtype.name as subtype,
    form.dateApproved as date,
    form.type as type,
    form.dateReviewed as dateReviewed,
    form.status as status
)
from InfoChange form
join form.project project
where form.reviewer.id = :userId and form.dateReviewed is not null
order by form.dateApproved desc
''',[userId: securityService.userId], cmd.args
        return [forms: forms, counts: getCounts(securityService.userId)]
    }

    def getFormForReview(String userId, Long id, ListType type) {
        def form = infoChangeService.getInfoForShow(id)

        if (!form) {
            throw new NotFoundException()
        }
        def activity = Activities.REVIEW
        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${InfoChange.WORKFLOW_ID}.${activity}"),
                User.load(userId),
        )
        domainStateMachineHandler.checkReviewer(id, userId, activity)
        def project = infoChangeService.findProject(form?.projectId)
        infoChangeService.projectUpdatedBefore(id, project as Map)
        return [
                form      : form,
                project   : project,
                counts    : getCounts(securityService.userId),
                workitemId: workitem ? workitem.id : null,
                prevId    : getPrevApprovalId(userId, id, type),
                nextId    : getNextApprovalId(userId, id, type),
        ]
    }

    def getFormForReview(String userId, Long id, ListType type, UUID workitemId) {
        def form = infoChangeService.getInfoForShow(id)

        if (!form) {
            throw new NotFoundException()
        }
        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, userId, activity)
        def project = infoChangeService.findProject(form?.projectId)
        infoChangeService.projectUpdatedBefore(id, project as Map)

        return [
                form      : form,
                project   : project,
                counts    : getCounts(securityService.userId),
                workitemId: workitemId,
                prevId    : getPrevApprovalId(userId, id, type),
                nextId    : getNextApprovalId(userId, id, type),
        ]
    }

    private Long getPrevApprovalId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.status = :status
and form.dateChecked < (select dateChecked from InfoChange where id = :id)
and form.reviewer.id = :userId
and form.dateReviewed is null
order by form.dateChecked asc
''', [userId: userId, id: id, status: State.CHECKED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.dateReviewed is not null
and form.reviewer.id = :userId
and form.dateReviewed > (select dateReviewed from InfoChange where id = :id)
order by form.dateReviewed asc
''', [userId: userId, id: id])
        }
    }

    private Long getNextApprovalId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.status = :status
and form.reviewer.id = :userId
and form.dateChecked > (select dateChecked from InfoChange where id = :id)
order by form.dateChecked asc
''', [userId: userId, id: id, status: State.CHECKED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.dateReviewed is not null
and form.reviewer.id = :userId
and form.dateReviewed < (select dateReviewed from InfoChange where id = :id)
order by form.dateApproved desc
''', [userId: userId, id: id])
        }
    }

    void createReview(String userId, AcceptCommand cmd, UUID workitemId) {
        InfoChange form = InfoChange.get(cmd.id)
        if (!domainStateMachineHandler.canReview(form)) {
            throw new BadRequestException()
        }
        domainStateMachineHandler.createReview(form, userId, cmd.comment, workitemId, Workitem.get(workitemId).from.id)
        form.dateReviewed = new Date()
        form.save()
    }
}
