package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectDepartmentOptionCommand
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

@Transactional
class ProjectDepartmentService {
    SecurityService securityService
    ApplicationService applicationService

    def list(ProjectDepartmentOptionCommand cmd) {
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
    project.delayTimes as delayTimes,
    (select count(*) from Review r where r.project.id = project.id and reportType = 3 and status = 'FINISHED' and conclusionOfUniversity = 'OK') as hasMid, 
    project.status as status
)
from Project project
join project.subtype subtype
join project.origin origin
where project.status <> 'CREATED'
'''
        cmd.departmentId = securityService.departmentId
        if (!cmd.criterion.isEmpty()) {
            sqlStr += " and ${cmd.criterion}"
        }
        sqlStr += " order by project.level, subtype.name, project.code"
        Project.executeQuery sqlStr, cmd.args
    }

    def getMiddleYears() {
        Project.executeQuery'''
select distinct new map(middleYear as middleYear)
from Project
where status <> 'CREATED' and department.id = :departmentId and middleYear is not null
order by middleYear
''', [departmentId: securityService.departmentId]
    }

    def getKnotYears() {
        Project.executeQuery'''
select distinct new map(knotYear as knotYear)
from Project
where status <> 'CREATED' and department.id = :departmentId
order by knotYear
''', [departmentId: securityService.departmentId]
    }

    Map getFormInfo(Long id) {
        def result = Project.executeQuery'''
select new map(
    application.id as id,
    project.id as projectId,
    project.principal.name as principalName,
    project.title as title,
    project.degree as degree,
    project.email as email,
    project.discipline as discipline,
    project.major as major,
    project.direction as direction,
    project.department.name as departmentName,
    project.office as office,
    project.phone as phone,
    project.name as name,
    project.level as level,
    application.status as status,
    project.urls as urls,
    project.status as projectStatus,
    project.code as code,
    project.dateStart as dateStarted,
    project.middleYear as middleYear,
    project.knotYear as knotYear,
    subtype.name as subtype,
    origin.name as origin,
    project.members as members,
    application.reportType as reportType
)
from Review application
right join application.project project
join project.subtype subtype
join project.origin origin
where project.id = :id
''', [id: id]
        if (result) {
            Map review = result[0]
            review['relationReportTypes'] = applicationService.reportTypes(id)
            return review
        } else {
            return null
        }
    }
}
