package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_WRITE")')
class ProjectController {
    ProjectService projectService

    def index() {
        renderJson projectService.list()
    }

    /**
     * 保存
     */
    def save(String teacherId) {
        def cmd = new ProjectCommand()
        bindData(cmd, request.JSON)
        cmd.principalId = teacherId
        def form = projectService.create(cmd)
        renderJson([id: form.id])
    }

    /**
     * 创建
     */
    def create() {
        renderJson projectService.getFormForCreate()
    }
}
