package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.hunt.cmd.ProjectDepartmentOptionCommand
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasAuthority("PERM_HUNT_OVERVIEW")')
class ProjectsController {
    FileTransferService fileTransferService
    ProjectsService projectsService

    def index(ProjectDepartmentOptionCommand cmd) {
        renderJson(projectsService.list(cmd))
    }

    def show(Long id) {
        renderJson projectsService.getFormInfo(id)
    }

    /**
     * 创建
     */
    def create() {
        renderJson projectsService.getFormForCreate()
    }

    /**
     * 保存
     */
    def save() {
        def cmd = new ProjectCommand()
        bindData(cmd, request.JSON)
        def form = projectsService.create(cmd)
        renderJson([id: form.id])
    }

    def update(Long id) {
        projectsService.updateMemo(id, request.JSON.memo)
        renderOk()
    }
}
