package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectDepartmentOptionCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_CHECK")')
class ProjectDepartmentController {
    ProjectDepartmentService projectDepartmentService
    ApplicationService applicationService
    TypeService typeService

    def index(String checkerId, ProjectDepartmentOptionCommand cmd) {
        renderJson projectDepartmentService.list(cmd)
    }

    def show(String checkerId, Long id) {
        renderJson projectDepartmentService.getFormInfo(id)
    }

    def create(Long checkerId) {
        renderJson([
                subtypes: typeService.getAllSubtypes(),
                middleYears: projectDepartmentService.middleYears,
                knotYears: projectDepartmentService.knotYears
        ])
    }
}
