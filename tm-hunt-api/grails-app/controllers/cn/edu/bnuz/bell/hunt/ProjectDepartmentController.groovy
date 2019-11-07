package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.InfoChangeCommand
import cn.edu.bnuz.bell.hunt.cmd.ProjectDepartmentOptionCommand
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_CHECK")')
class ProjectDepartmentController {
    ProjectDepartmentService projectDepartmentService
    InfoChangeService infoChangeService
    ProjectReviewerService projectReviewerService

    def index(String checkerId, ProjectDepartmentOptionCommand cmd) {
        renderJson projectDepartmentService.list(cmd)
    }

    def show(String checkerId, Long id) {
        renderJson projectDepartmentService.getFormInfo(id)
    }

    def save(String checkerId) {
        def cmd = new InfoChangeCommand()
        bindData(cmd, request.JSON)
        def form = infoChangeService.create(cmd)
        def smtcmd = new SubmitCommand()
        smtcmd.id = form.id
        smtcmd.title = cmd.title
        smtcmd.to = projectReviewerService.getApprovers()[0]?.id
        infoChangeService.submit(checkerId, smtcmd)
        renderJson([id: form.id])
    }

}
