package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class TypeController {
    TypeService typeService

    def index() {
        renderJson typeService.typeList()
    }

    def save() {
        def form = typeService.createType(request.JSON.name as String)
        renderJson([id: form.id])
    }

}
