package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import cn.edu.bnuz.bell.workflow.commands.RevokeCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_CHECK")')
class ApplicationCheckController {
    ApplicationCheckService applicationCheckService
    ProjectReviewerService projectReviewerService
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
            default:
                throw new BadRequestException()
        }

        show(checkerId, applicationCheckId, id, 'next')
    }

    def approvers(String checkerId, Long applicationCheckId) {
        renderJson projectReviewerService.getApprovers()
    }

    /**
     * 下载附件
     * @param checkerId 审核员ID
     * @param applicationCheckId 申请ID
     * @return
     */
    def attachments(String checkerId, Long applicationCheckId) {
        def review = Review.load(applicationCheckId)
        if (!review) {
            throw new NotFoundException()
        }
        if (review.department != Teacher.load(checkerId).department) {
            throw new ForbiddenException()
        }
        def basePath = "${filesPath}/${review.reviewTask.id}/${review.project.principal.id}"
        response.setHeader("Content-disposition",
                "attachment; filename=\"" + URLEncoder.encode("${review.project.subtype.name}-${review.project.name}-${review.project.principal.name}.zip", "UTF-8") + "\"")
        response.contentType = "application/zip"
        response.outputStream << ZipTools.zip(review, basePath)
        response.outputStream.flush()
    }
}
