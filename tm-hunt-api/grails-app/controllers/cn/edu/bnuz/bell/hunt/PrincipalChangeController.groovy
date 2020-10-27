package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.InfoChangeCommand
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_CHECK")')
class PrincipalChangeController {
    InfoChangeService infoChangeService
    FileTransferService fileTransferService
    ProjectReviewerService projectReviewerService

    def index(String checkerId) {
        renderJson infoChangeService.principalChangelist()
    }

    def show(String checkerId, Long id) {
        def form = infoChangeService.getInfoForShow(id)
        def project = infoChangeService.findProject(form?.projectId)
        infoChangeService.projectUpdatedBefore(id, project as Map)
        renderJson([
                form: form,
                project: project
        ])
    }


    /**
     * 保存
     */
    def save(String checkerId) {
        def cmd = new InfoChangeCommand()
        bindData(cmd, request.JSON)
        def form = infoChangeService.create(cmd)
        renderJson([id: form.id])
    }

    def create(String checkerId, Long projectId) {
        renderJson infoChangeService.getProjectForChange(projectId)
    }

    def patch(String checkerId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                infoChangeService.submit(checkerId, cmd)
                break
        }
        renderOk()
    }

    /**
     * 删除
     */
    def delete(Long id) {
        infoChangeService.delete(id)
        renderOk()
    }

    def upload(String checkerId) {
        String prefix = params.prefix
        renderJson ([file: fileTransferService.upload(prefix, "info-change/${checkerId}", request)])
    }

    def checkers(String checkerId) {
        renderJson projectReviewerService.getApprovers()
    }
}
