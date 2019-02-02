package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_CHECKER")')
class TaskCheckerController {
    ReviewTaskService reviewTaskService

    def index(String checkerId) {
        renderJson reviewTaskService.listForDepartment()
    }

    def show(String checkerId, Long id) {
        renderJson([task: reviewTaskService.getFormForShow(id), counts: reviewTaskService.countForDepartment(id)])
    }
}
