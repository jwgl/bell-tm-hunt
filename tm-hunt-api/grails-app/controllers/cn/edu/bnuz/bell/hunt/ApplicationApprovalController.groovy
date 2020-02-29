package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.ApprovalOperationCommand
import cn.edu.bnuz.bell.hunt.cmd.BatCommand
import cn.edu.bnuz.bell.hunt.cmd.FinishCommand
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ApplicationApprovalController {
    ApplicationApprovalService applicationApprovalService
    ApplicationAdministrationService applicationAdministrationService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String approverId, Long taskId, String type, Integer reportType) {
        ListType listType= ListType.valueOf(type)
        renderJson applicationApprovalService.list(approverId, taskId, listType, reportType ? reportType : 1)
    }

    def show(String approverId, Long applicationApprovalId, String id) {
        if (id == 'undefined') {
            renderJson applicationApprovalService.getFormForReview(approverId, applicationApprovalId)
        } else {
            renderJson applicationApprovalService.getFormForReview(approverId, applicationApprovalId, UUID.fromString(id))
        }
    }

    def save(String approverId) {
        BatCommand cmd = new BatCommand()
        bindData(cmd, request.JSON)
        switch (cmd.type) {
            case 'lock':
                applicationAdministrationService.lock(cmd)
                break
            case 'team':
                applicationAdministrationService.createExpertReview(cmd)
                break
            default:
                throw new BadRequestException()
        }

        renderOk()
    }

    def patch(String approverId, Long applicationApprovalId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.FINISH:
                def cmd = new FinishCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationApprovalId
                applicationApprovalService.finish(approverId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationApprovalId
                applicationApprovalService.reject(approverId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(approverId, applicationApprovalId, id, 'todo')
    }

    def update(String approverId, Long id) {
        ApprovalOperationCommand cmd = new ApprovalOperationCommand()
        bindData(cmd, request.JSON)
        if (cmd.removeExperts) {
            applicationAdministrationService.removeExpert(id) ? renderOk() : renderBadRequest()
        } else if (cmd.conclusionOfUniversity || cmd.conclusionOfProvince || cmd.opinionOfProvince || cmd.opinionOfUniversity) {
            applicationAdministrationService.updateConclusion(id, cmd)
            renderOk()
        } else {
            renderBadRequest()
        }
    }
}
