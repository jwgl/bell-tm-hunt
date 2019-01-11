package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.organization.Teacher
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
class InfoChangeApprovalService {
    DataAccessService dataAccessService
    InfoChangeService infoChangeService
    @Resource(name = 'projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler

    private static getCounts() {
        return [
                (ListType.TODO): InfoChange.countByStatus(State.CHECKED),
                (ListType.DONE): InfoChange.countByDateApprovedIsNotNull(),
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
    project.middleYear as middleYear,
    project.knotYear as knotYear,
    project.delayTimes as delayTimes,
    form.dateChecked as date,
    form.type as type,
    case when form.reviewer is null then false else true end as reviewer,
    form.dateReviewed as dateReviewed,
    form.status as status
)
from InfoChange form
join form.project project
where form.status = :status
order by form.dateChecked
''', [status: State.CHECKED], cmd.args
        return [forms: forms, counts: getCounts()]
    }

    def findDoneList(ListCommand cmd) {
        def forms = InfoChange.executeQuery '''
select new map(
    form.id as id,
    project.code as code,
    project.name as name,
    project.level as level,
    project.subtype.name as subtype,
    project.middleYear as middleYear,
    project.knotYear as knotYear,
    project.delayTimes as delayTimes,
    form.dateApproved as date,
    form.type as type,
    case when form.reviewer is null then false else true end as reviewer,
    form.dateReviewed as dateReviewed,
    form.status as status
)
from InfoChange form
join form.project project
where form.dateApproved is not null
order by form.dateApproved desc
''', cmd.args
        return [forms: forms, counts: getCounts()]
    }

    def getFormForApproval(String userId, Long id, ListType type) {
        def form = infoChangeService.getInfoForShow(id)

        if (!form) {
            throw new NotFoundException()
        }
        def activity = Activities.APPROVE
        def workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                WorkflowInstance.load(form.workflowInstanceId),
                WorkflowActivity.load("${InfoChange.WORKFLOW_ID}.${activity}"),
                User.load(userId),
        )
        if (!workitem) {
            workitem = Workitem.findByInstanceAndActivityAndToAndDateProcessedIsNull(
                    WorkflowInstance.load(form.workflowInstanceId),
                    WorkflowActivity.load("${InfoChange.WORKFLOW_ID}.${Activities.REVIEW}"),
                    User.load(userId),
            )
        }
        domainStateMachineHandler.checkReviewer(id, userId, activity)
        def project = infoChangeService.findProject(form?.projectId)
        infoChangeService.projectUpdatedBefore(id, project as Map)
        return [
                form      : form,
                project   : project,
                counts    : getCounts(),
                workitemId: workitem ? workitem.id : null,
                prevId    : getPrevApprovalId(userId, id, type),
                nextId    : getNextApprovalId(userId, id, type),
        ]
    }

    def getFormForApproval(String userId, Long id, ListType type, UUID workitemId) {
        def form = infoChangeService.getInfoForShow(id)

        if (!form) {
            throw new NotFoundException()
        }
        def activity = Workitem.get(workitemId).activitySuffix
        domainStateMachineHandler.checkReviewer(id, userId, activity)

        return [
                form      : form,
                counts    : getCounts(),
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
order by form.dateChecked asc
''', [id: id, status: State.CHECKED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.approver is not null
and form.dateApproved > (select dateApproved from InfoChange where id = :id)
order by form.dateApproved asc
''', [id: id])
        }
    }

    private Long getNextApprovalId(String userId, Long id, ListType type) {
        switch (type) {
            case ListType.TODO:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.status = :status
and form.dateChecked > (select dateChecked from InfoChange where id = :id)
order by form.dateChecked asc
''', [id: id, status: State.CHECKED])
            case ListType.DONE:
                return dataAccessService.getLong('''
select form.id
from InfoChange form
where form.approver is not null
and form.dateApproved < (select dateApproved from InfoChange where id = :id)
order by form.dateApproved desc
''', [id: id])
        }
    }

    void accept(String userId, AcceptCommand cmd, UUID workitemId) {
        InfoChange form = InfoChange.get(cmd.id)

        if (form.status != State.CHECKED) {
            return
        }
        domainStateMachineHandler.finish(form, userId, workitemId)
//        domainStateMachineHandler.accept(form, userId, Activities.APPROVE, cmd.comment, workitemId)
        def project = form.project
        form.type.each {
            switch (it) {
                case 1:
                    ChangeItem item = new ChangeItem(
                            infoChane: form,
                            key: 'principal',
                            content: project.principal.id
                    )
                    form.addToItems(item)
                    project.principal = form.principal
                    break
                case 2:
                    def review = Review.findByProjectAndReportTypeAndStatusAndConclusionOfUniversity(form.project, 3, State.FINISHED, Conclusion.OK)
                    if (!review) {
                        ChangeItem item = new ChangeItem(
                                infoChane: form,
                                key: 'middleYear',
                                content: project.middleYear
                        )
                        form.addToItems(item)
                        project.middleYear = form.middleYear
                        project.delayTimes ++
                    }
                    ChangeItem item = new ChangeItem(
                            infoChane: form,
                            key: 'knotYear',
                            content: project.knotYear
                    )
                    form.addToItems(item)
                    project.knotYear = form.knotYear
                    break
                case 3:
                    ChangeItem item = new ChangeItem(
                            infoChane: form,
                            key: 'name',
                            content: project.name
                    )
                    form.addToItems(item)
                    project.name = form.name
                    break
                case 4:
                    ChangeItem item = new ChangeItem(
                            infoChane: form,
                            key: 'content',
                            content: project.content
                    )
                    form.addToItems(item)
                    project.content = form.content
                    break
                case 5:
                    project.status = Status.CUTOUT
                    break
                case 6:
                    ChangeItem item = new ChangeItem(
                            infoChane: form,
                            key: 'achievements',
                            content: project.achievements
                    )
                    form.addToItems(item)
                    project.achievements = form.achievements
                    break
                case 7:
                    ChangeItem item = new ChangeItem(
                            infoChane: form,
                            key: 'members',
                            content: project.members
                    )
                    form.addToItems(item)
                    project.members = form.members
                    break
                case 8:
                    project.memo = "${project.memo}; ${form.other}"
            }
        }
        project.mainInfoForm = form.mainInfoForm
        form.approver = Teacher.load(userId)
        form.dateApproved = new Date()
        form.save()
    }

    void reject(String userId, RejectCommand cmd, UUID workitemId) {
        InfoChange form = InfoChange.get(cmd.id)
        if (form.status != State.CHECKED) {
            return
        }
        domainStateMachineHandler.reject(form, userId, cmd.comment, workitemId, form.checker.id)
        form.approver = Teacher.load(userId)
        form.dateApproved = new Date()
        form.checker = null
        form.dateChecked = null
        form.save()
    }

    void createReview(String userId, AcceptCommand cmd, UUID workitemId) {
        InfoChange form = InfoChange.get(cmd.id)
        if (!domainStateMachineHandler.canReview(form)) {
            throw new BadRequestException()
        }
        domainStateMachineHandler.createReview(form, userId, cmd.comment, workitemId, cmd.to)
        form.reviewer = Teacher.load(cmd.to)
        form.save()
    }
}
