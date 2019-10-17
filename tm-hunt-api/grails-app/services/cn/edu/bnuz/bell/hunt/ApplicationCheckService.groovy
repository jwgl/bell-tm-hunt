package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.security.UserLogService
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.hunt.cmd.ReviewCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import cn.edu.bnuz.bell.workflow.commands.RevokeCommand
import grails.gorm.transactions.Transactional

import javax.annotation.Resource

@Transactional
class ApplicationCheckService {
    ApplicationService applicationService
    @Resource(name='projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler
    DataAccessService dataAccessService
    UserLogService userLogService
    SecurityService securityService

    def list(String userId, Long taskId, ListType type, Integer reviewType) {
        switch (type) {
            case ListType.TODO:
                return findTodoList(userId, taskId, reviewType)
            case ListType.NEXT:
                return findNextList(userId, taskId, reviewType)
            case ListType.EXPR:
                return findFailList(userId, taskId, reviewType)
            case ListType.DONE:
                return findDoneList(userId, taskId ,reviewType)
            default:
                return allTypeList(userId, taskId)
        }
    }

    def allTypeList(String userId, Long taskId) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateSubmitted as date,
    project.title as title,
    project.degree as degree,
    project.major as major,
    project.office as office,
    project.phone as phone,
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) and application.status != 'CREATED'
and application.reviewTask.id = :taskId
order by application.dateSubmitted
''', [userId: userId, taskId: taskId]
    }

    def findTodoList(String userId, Long taskId, Integer reviewType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateSubmitted as date,
    project.title as title,
    project.degree as degree,
    project.major as major,
    project.office as office,
    project.phone as phone,
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
)  and application.status = :status
and application.reviewTask.id = :taskId
and application.reportType in (:reportTypes)
order by application.dateSubmitted
''', [userId: userId, status: State.SUBMITTED, taskId: taskId, reportTypes: reportTypes(taskId, reviewType)]
    }

    def findNextList(String userId, Long taskId, Integer reviewType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateChecked as date,
    project.title as title,
    project.degree as degree,
    project.major as major,
    project.office as office,
    project.phone as phone,
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.checker checker
where application.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
)
and application.status in (:status)
and application.reviewTask.id = :taskId
and application.reportType in (:reportTypes)
order by application.dateChecked desc
''', [userId: userId, status: [State.CHECKED, State.FINISHED], taskId: taskId, reportTypes: reportTypes(taskId, reviewType)]
    }

    def findDoneList(String userId, Long taskId, Integer reviewType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateChecked as date,
    project.title as title,
    project.degree as degree,
    project.major as major,
    project.office as office,
    project.phone as phone,
    application.conclusionOfUniversity as conclusionOfUniversity,
    application.conclusionOfProvince as conclusionOfProvince,
    application.status as status
)
from Review application
join application.project project
join project.subtype subtype
join project.origin origin
join application.checker checker
where application.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
)
and application.status = :status
and application.reviewTask.id = :taskId
and application.reportType in (:reportTypes)
order by application.dateChecked desc
''', [userId: userId, status: State.FINISHED, taskId: taskId, reportTypes: reportTypes(taskId, reviewType)]
    }

    def findFailList(String userId, Long taskId, Integer reviewType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateChecked as date,
    project.title as title,
    project.degree as degree,
    project.major as major,
    project.office as office,
    project.phone as phone,
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.checker checker
where application.department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
)
and application.status in (:status)
and application.reviewTask.id = :taskId
and application.reportType in (:reportTypes)
order by application.dateChecked desc
''', [userId: userId, status: [State.REJECTED, State.CLOSED], taskId: taskId, reportTypes: reportTypes(taskId, reviewType)]
    }

    void accept(String userId, AcceptCommand cmd, UUID workitemId) {
        Review application = Review.get(cmd.id)
        if (application.status == State.SUBMITTED) {
            domainStateMachineHandler.accept(application, userId, Activities.CHECK, cmd.comment, workitemId, cmd.to)
            application.checker = Teacher.load(userId)
            application.dateChecked = new Date()
            application.departmentOpinion = cmd.comment
            application.save()
        }
    }

    void review(String userId, ReviewCommand cmd, UUID workitemId) {
        Review application = Review.get(cmd.id)
        if (application.status == State.SUBMITTED) {
            domainStateMachineHandler.accept(application, userId, Activities.CHECK, cmd.comment, workitemId, cmd.to)
            application.checker = Teacher.load(userId)
            application.dateChecked = new Date()
            application.departmentOpinion = cmd.comment
            // 项目检查时单位需给单位意见
            if (application.reportType != 1) {
                application.departmentConclusion = cmd.review as Conclusion
            }
            application.save()
        }
    }

    void reject(String userId, RejectCommand cmd, UUID workitemId) {
        Review application = Review.get(cmd.id)
        if (application.status == State.SUBMITTED) {
            domainStateMachineHandler.reject(application, userId, Activities.CHECK, cmd.comment, workitemId)
            application.checker = Teacher.load(userId)
            application.dateChecked = new Date()
            application.departmentOpinion = cmd.comment
            application.save()
        }
    }

    void rollback(String userId, RevokeCommand cmd) {
        Review application = Review.get(cmd.id)
        if (!application) {
            throw new NotFoundException()
        }
        if (application.locked || !domainStateMachineHandler.canRollback(application)) {
            throw new ForbiddenException()
        }
        // 找到未审批的workitem
        def workitem = Workitem.findByInstanceAndActivityAndFromAndDateProcessedIsNull(
                WorkflowInstance.load(application.workflowInstanceId),
                WorkflowActivity.load("${Review.WORKFLOW_ID}.${Activities.APPROVE}"),
                User.load(userId))
        if (!workitem) {
            throw new BadRequestException()
        }
        workitem.delete()
        domainStateMachineHandler.rollback(application, userId, cmd.comment, workitem.id)
        if (cmd.comment) {
            userLogService.log(securityService.userId,securityService.ipAddress, 'ROLLBACK', application, "撤回理由：${cmd.comment}")
        }

        List<Workitem> result = Workitem.executeQuery'''
from Workitem where instance = :instance and activity = :activity and to = :to order by dateCreated desc
''', [instance: WorkflowInstance.load(application.workflowInstanceId), activity: WorkflowActivity.load('hunt.review.check'),
      to: User.load(userId)], [max: 1]
        if (!result) {
            throw new BadRequestException()
        }
        def revokeItem = result[0]
        revokeItem.setDateProcessed(null)
        revokeItem.setDateReceived(null)
        revokeItem.setNote(null)
        revokeItem.save()
        application.dateChecked = null
        application.save()
    }

    def getFormForReview(String userId, Long id, ListType type, UUID workitemId) {
        def form = applicationService.getFormInfo(id)
        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, userId, activity)

        return [
                form: form,
                workitemId: workitemId,
                prevId: getPrevReviewId(userId, id, type),
                nextId: getNextReviewId(userId, id, type),
        ]
    }

    def getFormForReview(String userId, Long id, ListType type) {
        def form = applicationService.getFormInfo(id)

        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${Review.WORKFLOW_ID}.${Activities.CHECK}"),
                User.load(userId),
        )
        domainStateMachineHandler.checkReviewer(id, userId, Activities.CHECK)
        def review = Review.load(id)

        return [
                form: form,
                workitemId: workitem ? workitem.id : null,
                prevId: getPrevReviewId(userId, id, type),
                nextId: getNextReviewId(userId, id, type),
                rollbackAble: review.status == State.CHECKED && !review.locked
        ]
    }

    private Long getPrevReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from Review form 
join form.reviewTask task
join form.department department
where current_date between task.startDate and task.endDate
and form.status = :status
and department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) 
and form.dateSubmitted < (select dateSubmitted from Review where id = :id)
order by form.dateSubmitted desc
''', [userId: userId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from Review form
join form.department department
where department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) 
and form.dateChecked is not null
and form.status in (:status)
and form.dateChecked > (select dateChecked from Review where id = :id)
order by form.dateChecked asc
''', [userId: userId, status: [State.REJECTED, State.CLOSED], id: id])
        }
    }

    private Long getNextReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from Review form 
join form.reviewTask task
join form.department department
where current_date between task.startDate and task.endDate
and form.status = :status
and department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) 
and form.dateSubmitted > (select dateSubmitted from Review where id = :id)
order by form.dateSubmitted asc
''', [userId: userId, id: id, status: State.SUBMITTED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from Review form
join form.department department
where department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId
) 
and form.dateChecked is not null
and form.status in (:status)
and form.dateChecked < (select dateChecked from Review where id = :id)
order by form.dateChecked desc
''', [userId: userId, status: [State.REJECTED, State.CLOSED], id: id])
        }
    }

    private static reportTypes (Long taskId, Integer reviewType) {
        def task = ReviewTask.load(taskId)
        if (task.type == ReviewType.APPLICATION) {
            return [1]
        }
        switch (reviewType) {
            case 0: return [2, 3]
            case 1: return [4]
        }
        return [0]
    }

    def counts(String userId, Long taskId, Integer reviewType) {
        def result = Review.executeQuery'''
select new map(
    sum (case when r.status = 'SUBMITTED' then 1 else 0 end) as countUncheck,
    sum (case when r.status in (:passStates) then 1 else 0 end) as countPass,
    sum (case when r.status in (:failStates) then 1 else 0 end) as countFail,
    sum (case when r.status = 'FINISHED' then 1 else 0 end) as countFinal
)
from Review r
join r.department department
where department = (
  select checker.department
  from Checker checker
  join checker.teacher teacher
  where teacher.id = :userId)
and r.reviewTask.id = :taskId
and r.reportType in (:reportTypes)
''', [userId: userId,
      taskId: taskId,
      reportTypes: reportTypes(taskId, reviewType),
      passStates: [State.FINISHED, State.CHECKED],
      failStates: [State.REJECTED, State.CLOSED]]
        return result ? result[0] : [:]
    }
}
