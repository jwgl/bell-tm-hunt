package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.BatCommand
import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.organization.DepartmentService
import cn.edu.bnuz.bell.organization.Teacher
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ProjectSelectController {
    DepartmentService departmentService
    TypeService typeService
    ProjectSelectService projectSelectService
    ApplicationService applicationService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

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

    def show(Long reviewTaskId, Long id) {
        renderJson applicationService.getFormInfo(id)
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

    /**
     * 删除
     */
    def delete(Long id) {
        projectSelectService.unCheck(id)
        renderOk()
    }
}
