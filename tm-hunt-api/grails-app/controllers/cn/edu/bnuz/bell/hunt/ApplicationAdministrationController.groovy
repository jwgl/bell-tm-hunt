package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.organization.Teacher
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ApplicationAdministrationController {
    ApplicationService applicationService
    ProjectSelectService projectSelectService
    ApplicationAdministrationService applicationAdministrationService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(Long taskApprovalId, ProjectOptionCommand cmd) {
        renderJson ([
                list: projectSelectService.listForAdministration(taskApprovalId, cmd.reportType),
                counts: projectSelectService.count(taskApprovalId),
                isCheckTime: ReviewTask.get(taskApprovalId)?.type == ReviewType.CHECK,
                existExpertReview: projectSelectService.existExpertReview(taskApprovalId, cmd.reportType)
        ])
    }

    def show(Long taskApprovalId, Long id) {
        renderJson applicationService.getFormInfo(id)
    }

    /**
     * 删除
     */
    def delete(Long id) {
        projectSelectService.unCheck(id)
        renderOk()
    }

    /**
     * 下载附件
     * @param approverId 审核员ID
     * @param applicationAdministrationId 申请ID
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

    /**
     * 专家评审
     * @param approverId 审核员ID
     * @param applicationAdministrationId 申请ID
     * @return
     */
    def expertReviews(String approverId, Long applicationAdministrationId) {
        renderJson applicationAdministrationService.expertReviews(applicationAdministrationId)
    }
}
