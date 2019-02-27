package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_DIRECTOR")')
class InfoChangeReviewController {
    ProjectReviewerService projectReviewerService
    InfoChangeReviewService infoChangeReviewService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String directorId, ListCommand cmd) {
        renderJson infoChangeReviewService.list(cmd)
    }

    def show(String directorId, Long infoChangeReviewId, String id, String type) {
        ListType listType = ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson infoChangeReviewService.getFormForReview(directorId, infoChangeReviewId, listType)
        } else {
            renderJson infoChangeReviewService.getFormForReview(directorId, infoChangeReviewId, listType, UUID.fromString(id))
        }
    }

    def patch(String directorId, Long infoChangeReviewId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.REVIEW:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = infoChangeReviewId
                infoChangeReviewService.createReview(directorId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(directorId, infoChangeReviewId, id, 'todo')
    }

    /**
     * 下载附件
     * @param checkerId 负责人ID
     * @param applicationId 申请ID
     * @return
     */
    def attachments(String directorId, Long infoChangeReviewId) {
        def infoChange = InfoChange.load(infoChangeReviewId)
        if (!infoChange) {
            throw new NotFoundException()
        }
        fileTransferService.download(infoChange, response)
    }

    def reviewers(String directorId) {
        renderJson projectReviewerService.approvers
    }
}
