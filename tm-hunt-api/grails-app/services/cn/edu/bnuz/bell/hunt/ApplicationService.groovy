package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.DepartmentService
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import grails.gorm.transactions.Transactional

import javax.annotation.Resource
import java.time.LocalDate

@Transactional
class ApplicationService {
    DepartmentService departmentService
    TypeService typeService
    SecurityService securityService
    @Resource(name='projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler

    def list(String userId) {
        Review.executeQuery'''
select new map(
    application.id as id,
    project.name as name,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    application.dateSubmitted as dateSubmitted,
    application.status as status
)
from Review application join application.project project
join project.subtype subtype
join project.origin origin
where project.principal.id = :userId and application.reportType = 1
''', [userId: userId]
    }

    def getFormInfo(Long id) {
        def result = Review.executeQuery'''
select new map(
    application.id as id,
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
    subtype.name as subtype,
    origin.name as origin,
    project.members as members,
    application.content as content,
    application.further as achievements,
    project.urls as urls,
    application.workflowInstance.id as workflowInstanceId
)
from Review application
join application.project project
join project.subtype subtype
join project.origin origin
where application.id = :id
''', [id: id]
        if (result) {
            return result[0]
        } else {
            return null
        }
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
                level: cmd.level as Level,
                status: Status.CREATED,
                urls: cmd.urls,
                subtype: Subtype.load(cmd.subtypeId),
                origin: Origin.load(cmd.originId),
                members: cmd.members
        )
        Review review = new Review(
                project: form,
                reportType: 1,
                reviewTask: ReviewTask.load(cmd.reviewTaskId),
                department: Department.load(cmd.departmentId),
                dateCreated: LocalDate.now(),
                status: domainStateMachineHandler.initialState,
                content: cmd.content,
                further: cmd.achievements
        )
        form.addToReview(review)
        if (!form.save()) {
            form.errors.each {
                println it
            }
        }
        domainStateMachineHandler.create(review, securityService.userId)
        return form
    }

    def submit(String userId, SubmitCommand cmd) {
        Review form = Review.get(cmd.id)

        if (!form) {
            throw new NotFoundException()
        }

        if (form.project.principal.id != userId) {
            throw new ForbiddenException()
        }

        if (!domainStateMachineHandler.canSubmit(form)) {
            throw new BadRequestException()
        }
        domainStateMachineHandler.submit(form, userId, cmd.to, cmd.comment, cmd.title)

        form.dateSubmitted = new Date()
        form.save()
    }

    Map getFormForCreate() {
        return [
                form: [
                        departmentId: securityService.departmentId,
                        level: Level.UNIVERSITY,
                ],
                departments: departmentService.allDepartments,
                subtypes: typeService.allSubtypes,
                origins: Origin.findAll()
        ]
    }

    def getCheckers() {
        Checker.executeQuery'''
select new map(t.id as id, t.name as name)
from Checker c
join c.department d
join c.teacher t
where d.id = :id
''', [id: securityService.departmentId]
    }
}
