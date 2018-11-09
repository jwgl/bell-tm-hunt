package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_CHECKER")')
class TaskCheckerController {
    ReviewTaskService reviewTaskService

    def index() {
        renderJson reviewTaskService.listForDepartment()
    }
}
