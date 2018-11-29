package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.DepartmentService
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.State
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
    DataAccessService dataAccessService

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
from Review application right join application.project project
join project.subtype subtype
join project.origin origin
where project.principal.id = :userId and application.reportType = 1
''', [userId: userId]
    }

    def getFormForEdit(Long id) {
        def result = Review.executeQuery'''
select new map(
    application.id as id,
    application.reviewTask.id as taskId,
    project.title as title,
    project.degree as degree,
    project.email as email,
    project.discipline as discipline,
    project.major as major,
    project.direction as direction,
    project.department.id as departmentId,
    project.office as office,
    project.phone as phone,
    project.name as name,
    project.level as level,
    application.status as status,
    project.urls as urls,
    subtype.id as subtypeId,
    origin.id as originId,
    project.members as members,
    application.content as content,
    application.further as achievements,
    application.mainInfoForm as mainInfoForm,
    application.proofFile as proofFile,
    application.summaryReport as summaryReport
)
from Review application
join application.project project
join project.subtype subtype
join project.origin origin
where application.id = :id
''', [id: id]
        if (result) {
            return [
                    form: result[0],
                    departments: departmentService.allDepartments,
                    subtypes: typeService.allSubtypes,
                    origins: Origin.findAll()
            ]
        } else {
            throw new BadRequestException()
        }
    }

    Map getFormInfo(Long id) {
        def result = Review.executeQuery'''
select new map(
    application.id as id,
    task.id as taskId,
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
    application.content as content,
    application.further as achievements,
    application.departmentConclusion as departmentConclusion,
    application.departmentOpinion as departmentOpinion,
    application.conclusionOfUniversity as conclusionOfUniversity,
    application.opinionOfUniversity as opinionOfUniversity,
    application.reportType as reportType,
    application.mainInfoForm as mainInfoForm,
    application.proofFile as proofFile,
    application.summaryReport as summaryReport,
    application.locked as locked,
    case when current_date between task.startDate and task.endDate then true else false end as isValidDate,
    application.workflowInstance.id as workflowInstanceId
)
from Review application
join application.project project
join project.subtype subtype
join project.origin origin
join application.reviewTask task
where application.id = :id
''', [id: id]
        if (result) {
            Map review = result[0]
            if (securityService.hasRole("ROLE_HUNT_ADMIN")) {
                review['expertReview'] = getExpertReview(review.id)
                review['period'] = getPeriod(review.id)
            } else {
                if (review.status != State.FINISHED.name()) {
                    // 审批结束前不让其他人看学校意见和结论
                    review.conclusionOfUniversity = null
                    review.opinionOfUniversity = null
                }
            }
            return review
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
                further: cmd.achievements,
                mainInfoForm: cmd.mainInfoForm,
                proofFile: cmd.proofFile
        )
        form.addToReview(review)
        if (!form.save()) {
            form.errors.each {
                println it
            }
        }
        domainStateMachineHandler.create(review, securityService.userId)
        return review
    }

    def update(Long id, ProjectCommand cmd) {
        def review = Review.load(id)
        if (!review) {
            throw new BadRequestException()
        }

        def form = review.project
        form.setTitle(cmd.title)
        form.setDegree(cmd.degree)
        form.setEmail(cmd.email)
        form.setDiscipline(cmd.discipline)
        form.setMajor(cmd.major)
        form.setDirection(cmd.direction)
        form.setDepartment(Department.load(cmd.departmentId))
        form.setOffice(cmd.office)
        form.setPhone(cmd.phone)
        form.setName(cmd.name)
        form.setLevel(cmd.level as Level)
        form.setStatus(Status.CREATED)
        form.setUrls(cmd.urls)
        form.setSubtype(Subtype.load(cmd.subtypeId))
        form.setOrigin(Origin.load(cmd.originId))
        form.setMembers(cmd.members)

        review.setContent(cmd.content)
        review.setFurther(cmd.achievements)
        review.setMainInfoForm(cmd.mainInfoForm)
        review.setProofFile(cmd.proofFile)
        form.save()

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

    private static getExpertReview(Long reviewId) {
        ExpertReview.executeQuery'''
select new map(
    t.id as teacherId,
    t.name as teacherName,
    e.opinion as opinion,
    e.conclusion as conclusion,
    e.dateReviewed as dateReviewed
)
from ExpertReview e
join e.review r
join e.expert t
where r.id = :id
''', [id: reviewId]
    }

    private getPeriod(Long reviewId) {
        dataAccessService.getInteger'''
select case when project.level = 'UNIVERSITY' then subtype.periodOfUniversity
  when project.level = 'CITY' then subtype.periodOfCity
  when project.level = 'PROVINCE' then subtype.periodOfProvince
  when project.level = 'NATION' then subtype.periodOfNation 
  else 0 end 
from Review application
join application.project project
join project.subtype subtype
where application.id = :id
''', [id: reviewId]
    }
}
