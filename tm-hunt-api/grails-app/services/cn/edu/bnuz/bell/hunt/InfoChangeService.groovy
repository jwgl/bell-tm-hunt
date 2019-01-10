package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.InfoChangeCommand
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.workflow.DomainStateMachineHandler
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import grails.gorm.transactions.Transactional
import org.springframework.boot.actuate.info.Info

import javax.annotation.Resource
import java.time.LocalDate

@Transactional
class InfoChangeService {
    SecurityService securityService
    @Resource(name='projectReviewStateMachine')
    DomainStateMachineHandler domainStateMachineHandler

    def list(String userId) {
        InfoChange.executeQuery'''
select new map(
    i.id as id,
    i.department as department,
    i.type as type,
    i.dateSubmitted as dateSubmitted,
    i.status as status,
    i.middleYear as middleYear,
    i.knotYear as knotYear,
    p.name as name,
    type.name as subtype,
    p.level as level,
    p.code as code,
    p.principal.name as principalName
)
from InfoChange i
join i.project p
join p.subtype type
where i.applicant.id = :userId
''', [userId: userId]
    }

    Map getInfoForShow(Long id) {
        def result = InfoChange.executeQuery'''
select new map(
    i.id as id,
    i.project.id as projectId,
    i.department.name as departmentName,
    i.type as type,
    i.dateSubmitted as dateSubmitted,
    i.status as status,
    i.middleYear as middleYear,
    i.knotYear as knotYear,
    i.members as members,
    i.content as content,
    i.achievements as achievements,
    i.other as other,
    i.mainInfoForm as mainInfoForm,
    i.name as name,
    i.locked as locked,
    i.dateReviewed as dateReviewed,
    type.name as subtype,
    p.level as level,
    p.code as code,
    p.principal.name as principalName,
    i.workflowInstance.id as workflowInstanceId
)
from InfoChange i
left join i.principal principal
join i.project p
join p.subtype type
where i.id = :id
''', [id: id]
        if (result) {
            Map map = result[0] as Map
            if (map.mainInfoForm) {
                map.mainInfoForm = getMainInfoFormName(map)
            }
            return map
        } else {
            return [:]
        }
    }

    def create(InfoChangeCommand cmd) {
        def project = Project.load(cmd.projectId)
        if (!project) {
            throw new BadRequestException('没有指定变更项目')
        }
        Teacher principal = cmd.principalId ? Teacher.load(cmd.principalId) : null
        InfoChange infoChange = new InfoChange(
                applicant: Teacher.load(securityService.userId),
                project: project,
                department: principal ? principal.department : project.department,
                type: cmd.type,
                principal: principal,
                middleYear: cmd.middleYear,
                knotYear: cmd.knotYear,
                name: cmd.name,
                content: cmd.content,
                achievements: cmd.achievements,
                members: cmd.members,
                other: cmd.other,
                mainInfoForm: cmd.mainInfoForm,
                status: domainStateMachineHandler.initialState,
                dateCreated: LocalDate.now()
        )
        if (!infoChange.save()) {
            infoChange.errors.each {
                println it
            }
        }
        domainStateMachineHandler.create(infoChange, securityService.userId)
        return infoChange
    }

    def update(Long id, InfoChangeCommand cmd) {
        def form = InfoChange.load(id)
        if (form.applicant.id != securityService.userId) {
            throw new ForbiddenException()
        }
        form.setType(cmd.type)
        form.setPrincipal(cmd.principalId ? Teacher.load(cmd.principalId) : null)
        form.setMiddleYear(cmd.middleYear)
        form.setKnotYear(cmd.knotYear)
        form.setName(cmd.name)
        form.setContent(cmd.content)
        form.setAchievements(cmd.achievements)
        form.setMembers(cmd.members)
        form.setOther(cmd.other)
        form.setMainInfoForm(cmd.mainInfoForm)
        form.save()
    }

    def projectForChange(String userId) {
        Project.executeQuery'''
select new map(
    project.id as id,
    project.name as name,
    project.code as code,
    project.level as level,
    project.subtype.name as subtype,
    project.middleYear as middleYear,
    project.knotYear as knotYear
)
from Project project
where project.principal.id = :userId and project.status = 'INHAND'
''', [userId: userId]
    }

    Map getFormForCreate(String userId) {
        return [
                form: [type: []],
                projects: projectForChange(userId)
        ]
    }

    def findProject(Long id) {
        def result = Project.executeQuery'''
select new map(
    id as id,
    principal.name as principalName,
    name as name,
    members as members,
    content as content,
    achievements as achievements,
    middleYear as middleYear,
    knotYear as knotYear,
    (select count(*) from Review where project.id = :id and reportType = 3 and status = 'FINISHED' and conclusionOfUniversity = 'OK') as hasMid, 
    mainInfoForm as mainInfoForm
)
from Project
where id = :id
''', [id: id]
        return result ? result[0] : null
    }

    static String getMainInfoFormName(Map form) {
        def ext = form.mainInfoForm.substring(form.mainInfoForm.lastIndexOf('.') + 1).toLowerCase()
        return "申报书-${form.name}-${ZipTools.levelLabel(form.level as String)}-${form.subtype}.${ext}"
    }

    def delete(Long id) {
        def form = InfoChange.get(id)
        if (form) {
            form.delete()
        }
    }

    def submit(String userId, SubmitCommand cmd) {
        InfoChange form = InfoChange.get(cmd.id)

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

    /**
     * 当审批通过，变更了原项目信息后，要在申请单中还原原项目信息以保持前后对照
     * @param infoChangeId
     * @param project
     */
    void projectUpdatedBefore(Long infoChangeId, Map project) {
        def infoChange = InfoChange.load(infoChangeId)
        if (infoChange && infoChange.status == State.FINISHED) {
            infoChange.items.each { item ->
                project[item.key] = item.content
            }
        }
    }
}
