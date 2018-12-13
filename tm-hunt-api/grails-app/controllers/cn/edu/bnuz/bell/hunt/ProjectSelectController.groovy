package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.BatCommand
import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import cn.edu.bnuz.bell.organization.DepartmentService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_IN_SCHOOL_TEACHER")')
class ProjectSelectController {
    DepartmentService departmentService
    TypeService typeService
    ProjectSelectService projectSelectService

    def index(Long reviewTaskId, String queryType, ProjectOptionCommand cmd) {
        switch (queryType) {
            case 'forCheck':
                renderJson projectSelectService.list(cmd)
                break
            case 'checked':
                renderJson ([
                    list: projectSelectService.list(reviewTaskId, cmd.reportType),
                    counts: projectSelectService.count(reviewTaskId)
                ])
                break
            default:
                renderBadRequest()
        }

    }

    def save(Long reviewTaskId) {
        BatCommand cmd = new BatCommand()
        bindData(cmd, request.JSON)
        if (cmd.reportType) {
            projectSelectService.createReview(reviewTaskId, cmd)
            renderOk()
        } else {
            renderBadRequest()
        }
    }

    def create(Long reviewTaskId) {
        renderJson([
                departments: departmentService.allDepartments,
                subtypes: typeService.getAllSubtypes(),
                middleYears: projectSelectService.middleYears,
                knotYears: projectSelectService.knotYears
        ])
    }
}
