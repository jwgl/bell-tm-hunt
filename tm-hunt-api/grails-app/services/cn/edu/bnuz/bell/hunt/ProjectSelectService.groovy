package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.BatCommand
import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.State
import grails.gorm.transactions.Transactional

import javax.annotation.Resource
import java.time.LocalDate

@Transactional
class ProjectSelectService {
    @Resource(name='projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler
    DataAccessService dataAccessService

    def list(ProjectOptionCommand cmd) {
        def sqlStr = '''
select new map(
    project.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    project.code as code,
    project.middleYear as middleYear,
    project.knotYear as knotYear,
    project.dateStart as dateStart,
    department.name as departmentName
)
from Project project
join project.subtype subtype
join project.origin origin
join project.department department
where project.status = 'INHAND'
'''
        if (!cmd.criterion.isEmpty()) {
            sqlStr += " and ${cmd.criterion}"
        }
        sqlStr += " order by project.level, subtype.name, project.code"
        Project.executeQuery sqlStr, cmd.args
    }

    def list(Long taskId, Integer reportType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    application.reviewTask.id as taskId,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    project.code as code,
    project.middleYear as middleYear,
    project.knotYear as knotYear,
    project.dateStart as dateStart,
    department.name as departmentName,
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where application.reportType = :reportType
and application.reviewTask.id = :taskId
order by project.level, subtype.name, project.code
''', [reportType: reportType, taskId: taskId]
    }

    def listForAdministration(Long taskId, Integer reportType) {
        Review.executeQuery'''
select new map(
    application.id as id,
    application.reviewTask.id as taskId,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    project.code as code,
    principal.name as principalName,
    department.name as departmentName,
    application.departmentOpinion as departmentOpinion,
    application.opinionOfUniversity as opinionOfUniversity,
    application.opinionOfProvince as opinionOfProvince,
    application.conclusionOfUniversity as conclusionOfUniversity,
    application.status as status,
    case when project.level = 'PROVINCE' then application.conclusionOfProvince else application.conclusionOfUniversity end as conclusion,
    round (sum (case when expertReview.dateReviewed is not null and expertReview.conclusion != '弃权' then expertReview.value end)/
        sum (case when expertReview.dateReviewed is not null and expertReview.conclusion != '弃权' then 1 else 0 end)),
    sum (case when expertReview.dateReviewed is not null and expertReview.conclusion = '同意' then 1 else 0 end) as countOk,
    sum (case when expertReview.dateReviewed is not null and expertReview.conclusion = '不同意' then 1 else 0 end) as countVeto,
    sum (case when expertReview.dateReviewed is not null and expertReview.conclusion = '弃权' then 1 else 0 end) as countWaiver,
    sum (case when expertReview.dateReviewed is null then 1 else 0 end) as countNull,
    sum (expertReview.value) as totalScore
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
join project.principal principal
left join application.expertReview expertReview
where application.reportType = :reportType
and application.status in (:passStates)
and application.reviewTask.id = :taskId
group by application.id, application.reviewTask.id, project.name, project.level, subtype.name, project.code, principal.name, department.name, application.status
order by project.level, subtype.name, project.code
''', [reportType: reportType, passStates: [State.FINISHED, State.CHECKED], taskId: taskId]
    }

    def getMiddleYears() {
        Project.executeQuery'''
select distinct new map(middleYear as middleYear)
from Project
where status = 'INHAND' and middleYear is not null
order by middleYear
'''
    }

    def getKnotYears() {
        Project.executeQuery'''
select distinct new map(knotYear as knotYear)
from Project
where status = 'INHAND'
order by knotYear
'''
    }

    def createReview(Long taskId, BatCommand cmd) {
        def task = ReviewTask.load(taskId)
        cmd.ids.each { id ->
            def form = Project.load(id)
            if (form && task) {
                Review review = new Review(
                        project: form,
                        reportType: cmd.reportType,
                        reviewTask: task,
                        department: form.department,
                        dateCreated: LocalDate.now(),
                        status: domainStateMachineHandler.initialState
                )
                if (!review.save()) {
                    review.errors.each {
                        println it
                    }
                }
            }
        }
    }

    def count(Long taskId) {
        Review.executeQuery'''
select new map(e.reportType as reportType, count(*) as value) from Review e where e.reviewTask.id = :taskId group by e.reportType
''', [taskId: taskId]
    }

    def unCheck(Long id) {
        def review = Review.load(id)
        if (review && review.status == State.CREATED) {
            review.delete()
        }
    }

    def existExpertReview(Long taskId, Integer reportType) {
        def countExpertReview = dataAccessService.getInteger'''
select count(*) from Review a join a.expertReview e where a.reportType = :reportType and a.reviewTask.id = :taskId
''', [reportType: reportType, taskId: taskId]
        return countExpertReview > 0
    }
}
