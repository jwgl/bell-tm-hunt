package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ReviewTaskCommand
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ReviewTaskController {
    ReviewTaskService reviewTaskService

    def index() {
        renderJson reviewTaskService.list()
    }

    def save() {
        def cmd = new ReviewTaskCommand()
        bindData(cmd, request.JSON)
        def form = reviewTaskService.create(cmd)
        renderJson([id: form.id])
    }

    def show(Long id) {
        renderJson reviewTaskService.getFormForShow(id)
    }
}
