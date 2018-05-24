package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.DepartmentService
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional

@Transactional
class ProjectService {
    DepartmentService departmentService
    TypeService typeService
    SecurityService securityService

    def list() {
        Project.executeQuery'''
select new map(
    project.name as name,
    subtype.level as level,
    subtype.name as subtype,
    origin.name as origin,
    project.status as status
)
from Project project
join project.subtype subtype
join project.origin origin
'''
    }

    def create(ProjectCommand cmd) {
        def form = new Project(
                principal: Teacher.load(cmd.principalId),
                title: cmd.title,
                degree: cmd.degree,
                email: cmd.email,
                discipline: cmd.discipline,
                major: cmd.major,
                direction: cmd.direction,

                department: Department.load(cmd.departmentId),
                office: cmd.office,
                phone: cmd.phone,

                name: cmd.name,
                status: Status.CREATED,
                level: cmd.level as Level,
                urls: cmd.urls,
                subtype: Subtype.load(cmd.subtypeId),
                originId: Origin.load(cmd.originId),
                members: cmd.members,
                content: cmd.content,
                achievements: cmd.achievements
        )
        if (!form.save()) {
            form.errors.each {
                println it
            }
        }
        return form
    }

    Map getFormForCreate() {
        return [
                form: [
                        departmentId: securityService.departmentId,
                        level: Level.UNIVERSITY,
                ],
                departments: departmentService.allDepartments,
                subtypes: typeService.allSubtypes
        ]
    }
}
