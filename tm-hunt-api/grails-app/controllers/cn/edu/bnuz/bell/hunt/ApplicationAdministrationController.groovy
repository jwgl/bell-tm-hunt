package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ApplicationAdministrationController {
    ApplicationService applicationService
    ProjectSelectService projectSelectService
    ApplicationAdministrationService applicationAdministrationService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(Long taskApprovalId, ProjectOptionCommand cmd) {
        renderJson ([
                list: applicationAdministrationService.listForAdministration(taskApprovalId, cmd.reportType),
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
     * 专家评审
     * @param approverId 审核员ID
     * @param applicationAdministrationId 申请ID
     * @return
     */
    def expertReviews(String approverId, Long applicationAdministrationId) {
        renderJson applicationAdministrationService.expertReviews(applicationAdministrationId)
    }
}
