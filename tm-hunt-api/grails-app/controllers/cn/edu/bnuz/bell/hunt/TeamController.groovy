package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class TeamController {
    ExpertService expertService

    def index() {
        renderJson expertService.loadTeam()
    }

    def save() {
        def ids = request.JSON.ids
        expertService.makeTeam(ids)
        renderOk()
    }

    def delete(Integer id) {
        expertService.drop(id)
        renderOk()
    }
}
