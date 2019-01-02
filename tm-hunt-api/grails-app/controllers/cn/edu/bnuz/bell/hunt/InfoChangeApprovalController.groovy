package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.ListCommand
import cn.edu.bnuz.bell.workflow.ListType
import cn.edu.bnuz.bell.workflow.commands.AcceptCommand
import cn.edu.bnuz.bell.workflow.commands.RejectCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class InfoChangeApprovalController {
	InfoChangeApprovalService infoChangeApprovalService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String approverId, ListCommand cmd) {
        renderJson infoChangeApprovalService.list(cmd)
    }

    def show(String approverId, Long infoChangeApprovalId, String id, String type) {
        ListType listType = ListType.valueOf(type)
        if (id == 'undefined') {
            renderJson infoChangeApprovalService.getFormForApproval(approverId, infoChangeApprovalId, listType)
        } else {
            renderJson infoChangeApprovalService.getFormForApproval(approverId, infoChangeApprovalId, listType, UUID.fromString(id))
        }
    }

    def patch(String approverId, Long infoChangeApprovalId, String id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.ACCEPT:
                def cmd = new AcceptCommand()
                bindData(cmd, request.JSON)
                cmd.id = infoChangeApprovalId
                infoChangeApprovalService.accept(approverId, cmd, UUID.fromString(id))
                break
            case Event.REJECT:
                def cmd = new RejectCommand()
                bindData(cmd, request.JSON)
                cmd.id = infoChangeApprovalId
                infoChangeApprovalService.reject(approverId, cmd, UUID.fromString(id))
                break
            default:
                throw new BadRequestException()
        }

        show(approverId, infoChangeApprovalId, id, 'todo')
    }

    /**
     * 下载附件
     * @param checkerId 负责人ID
     * @param applicationId 申请ID
     * @return
     */
    def attachments(String approverId, Long infoChangeApprovalId) {
        def infoChange = InfoChange.load(infoChangeApprovalId)
        if (!infoChange) {
            throw new NotFoundException()
        }

        def basePath = "${filesPath}/info-change/${infoChange.project.principal.id}"
        response.setHeader("Content-disposition",
                "attachment; filename=\"" + URLEncoder.encode("${infoChange.project.subtype.name}-${infoChange.project.name}-${infoChange.project.principal.name}.zip", "UTF-8") + "\"")
        response.contentType = "application/zip"
        response.outputStream << ZipTools.zip(infoChange, basePath)
        response.outputStream.flush()
    }
}
