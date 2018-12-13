package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.BatCommand
import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import grails.gorm.transactions.Transactional

import javax.annotation.Resource
import java.time.LocalDate

@Transactional
class ProjectSelectService {
    @Resource(name='projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler

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
}
