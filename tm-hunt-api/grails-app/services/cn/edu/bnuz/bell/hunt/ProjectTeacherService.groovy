package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

@Transactional
class ProjectTeacherService {
    SecurityService securityService
    ProjectsService projectsService

    def list() {
        def list = Project.executeQuery '''
select new map(
    project.id as id,
    project.name as name,
    department.name as departmentName,
    parent.name as parentName,
    subtype.name as subtype,
    project.principal.name as principalName,
    project.title as title,
    project.degree as degree,
    project.office as office,
    project.phone as phone,
    project.level as level,
    origin.name as origin,
    project.code as code,
    project.middleYear as middleYear,
    project.knotYear as knotYear,
    project.dateStart as dateStart,
    project.delayTimes as delayTimes,
    project.dateFinished as dateFinished,
    (select count(*) from Review r where r.project.id = project.id and reportType = 3 and status = 'FINISHED' and conclusionOfUniversity = 'OK') as hasMid, 
    project.status as status
)
from Project project
join project.subtype subtype
join subtype.parent parent
join project.department department
join project.origin origin
where project.principal.id = :userId
''', [userId: securityService.userId]
    }

    Map getFormInfo(Long id) {
        if (Teacher.load(securityService.userId) != Project.load(id)?.principal) {
            throw new ForbiddenException()
        }
        def form = projectsService.getFormInfo(id)
        return form
    }
}
