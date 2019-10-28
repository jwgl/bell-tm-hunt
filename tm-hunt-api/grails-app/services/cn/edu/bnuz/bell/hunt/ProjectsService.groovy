package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.hunt.cmd.ProjectDepartmentOptionCommand
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.DepartmentService
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

import java.time.LocalDate

@Transactional
class ProjectsService {
    SecurityService securityService
    TypeService typeService
    ApplicationService applicationService
    DepartmentService departmentService

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
    project.dateFinished as dateFinished,
    (select count(*) from Review r where r.project.id = project.id and reportType = 3 and status = 'FINISHED' and conclusionOfUniversity = 'OK') as hasMid, 
    project.status as status
)
from Project project
join project.subtype subtype
join project.origin origin
where project.status <> 'CREATED'
'''
        if (!cmd.criterion.isEmpty()) {
            sqlStr += " and ${cmd.criterion}"
        }

        sqlStr += " order by project.level, subtype.name, project.code"
        def list = Project.executeQuery sqlStr, cmd.args
        return [
                list: list,
                fund: fund(cmd),
                subtypes: typeService.allSubtypes,
                middleYears: middleYears,
                knotYears: knotYears,
                departments: departmentService.allDepartments,
                createAble: securityService.hasRole('ROLE_HUNT_ADMIN')
        ]
    }

    def updateMemo(Long id, String memo) {
        def project = Project.load(id)
        if (!project) {
            throw new NotFoundException()
        }
        project.setMemo(memo)
        project.save()
    }

    def getMiddleYears() {
        Project.executeQuery'''
select distinct new map(middleYear as middleYear)
from Project
where status <> 'CREATED' and middleYear is not null
order by middleYear
'''
    }

    def getKnotYears() {
        Project.executeQuery'''
select distinct new map(knotYear as knotYear)
from Project
where status <> 'CREATED'
order by knotYear
'''
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
    project.department.id as departmentId,
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
    project.memo as memo,
    application.reportType as reportType
)
from Review application
right join application.project project
join project.subtype subtype
join project.origin origin
where project.id = :id
''', [id: id]
        if (result) {
            Map review = result[0] as Map
            // 只有指定权限的用户可以查看备注
            if (!securityService.hasPermission('PERM_HUNT_OVERVIEW')) {
                review.memo = null
            }
            review['relationReportTypes'] = applicationService.reportTypes(id)
            return review
        } else {
            return null
        }
    }

    Map getFormForCreate() {
        return [
                form: [level: Level.UNIVERSITY],
                departments: departmentService.allDepartments,
                subtypes: typeService.allSubtypesWithPeriod,
                origins: Origin.findAll()
        ]
    }

    def create(ProjectCommand cmd) {
        def form = new Project(
                principal: Teacher.load(cmd.principalId),
                department: Department.load(cmd.departmentId),
                name: cmd.name,
                level: cmd.level as Level,
                status: Status.INHAND,
                subtype: Subtype.load(cmd.subtypeId),
                origin: Origin.load(cmd.originId),
                phone: '/',
                delayTimes: 0,
                title: '/',
                degree: '/',
                email: '/',
                dateStart: LocalDate.parse(cmd.dateStarted),
                middleYear: cmd.middleYear,
                knotYear: cmd.knotYear,
                code: cmd.code
        )
        if (!form.save()) {
            form.errors.each {
                println it
            }
        }
        return form
    }

    def fund(ProjectDepartmentOptionCommand cmd) {
        def sqlStr = '''
select  new map(
f.type as type,
sum(f.amount) as total
)
from Fund f 
join f.project project
join project.subtype subtype
where project.status <> 'CREATED'
'''
        if (!cmd.criterion.isEmpty()) {
            sqlStr += " and ${cmd.criterion}"
        }

        sqlStr += " group by f.type order by f.type desc"
        Fund.executeQuery sqlStr, cmd.args
    }
}
