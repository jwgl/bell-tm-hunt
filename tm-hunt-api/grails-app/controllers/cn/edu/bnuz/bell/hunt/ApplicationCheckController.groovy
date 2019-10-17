package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import cn.edu.bnuz.bell.workflow.commands.RevokeCommand
import cn.edu.bnuz.bell.hunt.cmd.ReviewCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_CHECK")')
class ApplicationCheckController {
    ApplicationCheckService applicationCheckService
    ProjectReviewerService projectReviewerService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String checkerId, Long taskId, String type, Integer reviewType) {
        if (type && type != 'null') {
            ListType listType= ListType.valueOf(type)
            renderJson ([
                    list: applicationCheckService.list(checkerId, taskId, listType, reviewType),
                    counts: applicationCheckService.counts(checkerId, taskId, reviewType)
            ])
        } else {
            renderBadRequest()
        }
    }

    def show(String checkerId, Long applicationCheckId, String id, String type) {
        ListType listType= ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson applicationCheckService.getFormForReview(checkerId, applicationCheckId, listType)
        } else {
            renderJson applicationCheckService.getFormForReview(checkerId, applicationCheckId, listType, UUID.fromString(id))
        }
    }

    def patch(String checkerId, Long applicationCheckId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationCheckId
                applicationCheckService.accept(checkerId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationCheckId
                applicationCheckService.reject(checkerId, cmd, UUID.fromString(id))
                break
            case Event.ROLLBACK:
                def cmd = new RevokeCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationCheckId
                applicationCheckService.rollback(checkerId, cmd)
                break
            case Event.REVIEW:
                def cmd = new ReviewCommand()
                bindData(cmd, request.JSON)
                cmd.id = applicationCheckId
                applicationCheckService.review(checkerId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(checkerId, applicationCheckId, id, 'next')
    }

    def approvers(String checkerId, Long applicationCheckId) {
        renderJson projectReviewerService.getApprovers()
    }
}
