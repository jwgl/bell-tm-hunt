package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.CheckerCommand
import cn.edu.bnuz.bell.organization.DepartmentService
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class CheckerController {
    CheckerService checkerService
    DepartmentService departmentService

    def index() {
        renderJson checkerService.list()
    }

    def save() {
        def cmd = new CheckerCommand()
        bindData(cmd, request.JSON)
        def form = checkerService.create(cmd)
        renderJson([id: form.id])
    }

    def delete(Long id) {
        checkerService.delete(id)
        renderOk()
    }

    /**
     * 创建
     */
    def create() {
        renderJson([
                departments: departmentService.teachingDepartments
        ])
    }
}
