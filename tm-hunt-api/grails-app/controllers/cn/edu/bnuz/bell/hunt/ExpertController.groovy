package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ExpertCommand
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ExpertController {
    ExpertService expertService

    def index() {
        renderJson expertService.list()
    }

    def save() {
        def cmd = new ExpertCommand()
        bindData(cmd, request.JSON)
        def form = expertService.create(cmd)
        renderJson([id: form.id])
    }

    def patch(Long id, Boolean op) {
        expertService.update(id, op)
        renderOk()
    }
}
