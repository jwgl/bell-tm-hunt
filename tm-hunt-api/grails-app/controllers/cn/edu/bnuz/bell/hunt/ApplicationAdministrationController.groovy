package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.organization.Teacher
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ApplicationAdministrationController {
    ApplicationService applicationService
    @Value('${bell.teacher.filesPath}')
    String filesPath
    def index() { }

    def show(Long reviewTaskId, Long id) {
        renderJson applicationService.getFormInfo(id)
    }

    /**
     * 下载附件
     * @param approverId 审核员ID
     * @param applicationCheckId 申请ID
     * @return
     */
    def attachments(String approverId, Long applicationAdministrationId) {
        def review = Review.load(applicationAdministrationId)
        if (!review) {
            throw new NotFoundException()
        }
        if (review.department != Teacher.load(approverId).department) {
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
