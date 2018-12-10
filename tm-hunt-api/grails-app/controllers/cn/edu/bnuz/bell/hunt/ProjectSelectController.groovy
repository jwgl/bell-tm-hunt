package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import cn.edu.bnuz.bell.organization.DepartmentService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_IN_SCHOOL_TEACHER")')
class ProjectSelectController {
    DepartmentService departmentService
    TypeService typeService
    ProjectSelectService projectSelectService

    def index(Long taskId, ProjectOptionCommand cmd) {
        renderJson projectSelectService.list(cmd)
    }

    def create(Long taskId) {
        renderJson([
                departments: departmentService.allDepartments,
                subtypes: typeService.getAllSubtypes(),
                middleYears: projectSelectService.middleYears,
                knotYears: projectSelectService.knotYears
        ])
    }
}
