package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class TaskApprovalController {
    ReviewTaskService reviewTaskService

    def index(Long taskId) {
        if (taskId) {
            renderJson reviewTaskService.countForApproval(taskId)
        } else {
            renderJson reviewTaskService.listForApproval()
        }
    }

    def show(Long id) {
        renderJson reviewTaskService.getFormForShow(id)
    }
}
