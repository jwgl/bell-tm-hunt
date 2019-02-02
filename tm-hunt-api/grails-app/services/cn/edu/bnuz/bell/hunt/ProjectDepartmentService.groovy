package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.hunt.cmd.ProjectDepartmentOptionCommand
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

@Transactional
class ProjectDepartmentService {
    SecurityService securityService
    ProjectsService projectsService
    TypeService typeService

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
        def list = Project.executeQuery sqlStr, cmd.args
        return [
                list: list,
                subtypes: typeService.getAllSubtypes(),
                middleYears: middleYears,
                knotYears: knotYears
        ]
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
        def form = projectsService.getFormInfo(id)
        if (securityService.departmentId != form.departmentId) {
            throw new ForbiddenException()
        }
        return form
    }
}
