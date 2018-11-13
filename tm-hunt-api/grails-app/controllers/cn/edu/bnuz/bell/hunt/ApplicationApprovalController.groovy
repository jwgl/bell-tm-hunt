package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.cmd.LockCommand
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ApplicationApprovalController {
    ApplicationApprovalService applicationApprovalService

    def index(String approverId, Long taskId, String type) {
        ListType listType= ListType.valueOf(type)
        renderJson applicationApprovalService.list(approverId, taskId, listType)
    }

    def show(String approverId, Long applicationApprovalId, String id, String type) {
        ListType listType= ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson applicationApprovalService.getFormForReview(approverId, applicationApprovalId, listType)
        } else {
            renderJson applicationApprovalService.getFormForReview(approverId, applicationApprovalId, listType, UUID.fromString(id))
        }
    }

    def save(String approverId) {
        LockCommand cmd = new LockCommand()
        bindData(cmd, request.JSON)
        applicationApprovalService.lock(approverId, cmd)
        renderOk()
    }

    def patch(String approverId, Long applicationApprovalId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationApprovalId
                applicationApprovalService.accept(approverId, cmd, UUID.fromString(id))
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

}
