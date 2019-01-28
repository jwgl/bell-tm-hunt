package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectDepartmentOptionCommand
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasAuthority("PERM_HUNT_OVERVIEW")')
class ProjectsController {
    ProjectsService projectsService

    def index(ProjectDepartmentOptionCommand cmd) {
        renderJson(projectsService.list(cmd))
    }

    def show(Long id) {
        renderJson projectsService.getFormInfo(id)
    }

    def update(Long id) {
        projectsService.updateMemo(id, request.JSON.memo)
        renderOk()
    }
}
