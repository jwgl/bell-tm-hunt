package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.BatCommand
import cn.edu.bnuz.bell.hunt.cmd.FinishCommand
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.WorkflowActivity
import cn.edu.bnuz.bell.workflow.WorkflowInstance
import cn.edu.bnuz.bell.workflow.Workitem
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import grails.gorm.transactions.Transactional

import javax.annotation.Resource
import java.time.LocalDate

@Transactional
class ApplicationApprovalService {
    ApplicationService applicationService
    @Resource(name='projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler
    DataAccessService dataAccessService

    def list(String userId, Long taskId, ListType type, Integer reportType) {
        switch (type) {
            case ListType.TODO:
                return findTodoList(userId, taskId, reportType)
            case ListType.DONE:
                return findDoneList(userId, taskId, reportType)
            case ListType.EXPR:
                return findFailList(userId, taskId, reportType)
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
    (select count(*) from ExpertReview where review = application) as countExpert, 
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where application.status in (:status)'
and application.reviewTask.id = :taskId
order by application.dateChecked
''', [userId: userId, status: [State.CHECKED, State.APPROVED], taskId: taskId]
    }

    def findTodoList(String userId, Long taskId, Integer reportType) {
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
    department.name as departmentName,
    application.locked as locked,
    application.conclusionOfUniversity as conclusionOfUniversity,
    (select count(*) from ExpertReview where review = application) as countExpert, 
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where application.status = :status
and application.reportType = :reportType
and application.reviewTask.id = :taskId
order by application.dateChecked
''', [status: State.CHECKED, taskId: taskId, reportType: reportType]
    }

    def findDoneList(String userId, Long taskId, Integer reportType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateApproved as date,
    project.title as title,
    project.degree as degree,
    project.major as major,
    project.office as office,
    project.phone as phone,
    department.name as departmentName,
    application.locked as locked,
    application.conclusionOfUniversity as conclusionOfUniversity,
    (select count(*) from ExpertReview where review = application) as countExpert, 
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where application.status = 'FINISHED' and application.conclusionOfUniversity = 'OK'
and application.reportType = :reportType
and application.reviewTask.id = :taskId
order by application.dateApproved desc
''', [taskId: taskId, reportType: reportType]
    }

    def findFailList(String userId, Long taskId, Integer reportType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateApproved as date,
    project.title as title,
    project.degree as degree,
    project.major as major,
    project.office as office,
    project.phone as phone,
    department.name as departmentName,
    application.locked as locked,
    application.conclusionOfUniversity as conclusionOfUniversity,
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where application.status = 'FINISHED' and (application.conclusionOfUniversity = 'VETO' or application.conclusionOfUniversity = 'DELAY')
and application.reviewTask.id = :taskId
and application.reportType = :reportType
order by application.dateApproved desc
''', [taskId: taskId, reportType: reportType]
    }

    void finish(String userId, FinishCommand cmd, UUID workitemId) {
        Review application = Review.get(cmd.id)
        if (!application) {
            throw new NotFoundException()
        }
        if (!domainStateMachineHandler.canFinish(application)) {
            throw new BadRequestException()
        }
        domainStateMachineHandler.finish(application, userId, workitemId)
        application.approver = Teacher.load(userId)
        application.dateApproved = new Date()
        if ((application.project.level == Level.PROVINCE && application.conclusionOfProvince == Conclusion.OK) ||
            (application.project.level == Level.UNIVERSITY && application.conclusionOfUniversity == Conclusion.OK)) {
            switch (application.reportType) {
                case 1:
                    application.project.setStatus(Status.INHAND)
                    break
                case 4:
                    application.project.setStatus(Status.FINISHED)
            }
            application.project.save()
        } else if (application.conclusionOfProvince == Conclusion.DELAY){
            // 如果是中期暂缓，中期和结项时间都延期，如果是结项暂缓，只延期结题
            switch (application.reportType) {
                case 3:
                    application.project.setMiddleYear(application.project.middleYear + 1)
                    application.project.setKnotYear(application.project.knotYear + 1)
                    application.project.setDelayTimes(application.project.delayTimes + 1)
                    break
                case 4:
                    application.project.setKnotYear(application.project.knotYear + 1)
                    application.project.setDelayTimes(application.project.delayTimes + 1)
            }
        }
        application.save()
    }

    void reject(String userId, RejectCommand cmd, UUID workitemId) {
        Review application = Review.get(cmd.id)
        domainStateMachineHandler.reject(application, userId, cmd.comment, workitemId, application.checker.id)
        application.approver = Teacher.load(userId)
        application.dateApproved = new Date()
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
        if (!form) {
            throw new BadRequestException()
        }
        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${Review.WORKFLOW_ID}.${Activities.APPROVE}"),
                User.load(userId),
        )
        domainStateMachineHandler.checkReviewer(id, userId, Activities.APPROVE)
        return [
                form: form,
                workitemId: workitem ? workitem.id : null,
                prevId: getPrevReviewId(userId, id, type),
                nextId: getNextReviewId(userId, id, type)
        ]
    }

    private Long getPrevReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from Review form 
join form.reviewTask task
where current_date between task.startDate and task.endDate
and form.status = :status
and form.dateChecked < (select dateChecked from Review where id = :id)
order by form.dateChecked desc
''', [id: id, status: State.CHECKED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from Review form
where form.dateApproved is not null
and form.dateApproved > (select dateApproved from Review where id = :id)
order by form.dateApproved asc
''', [id: id])
        }
    }

    private Long getNextReviewId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from Review form 
join form.reviewTask task
where current_date between task.startDate and task.endDate
and form.status = :status
and form.dateChecked > (select dateChecked from Review where id = :id)
order by form.dateChecked asc
''', [id: id, status: State.CHECKED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from Review form
where form.dateApproved is not null
and form.dateApproved < (select dateApproved from Review where id = :id)
order by form.dateApproved desc
''', [id: id])
        }
    }
}
