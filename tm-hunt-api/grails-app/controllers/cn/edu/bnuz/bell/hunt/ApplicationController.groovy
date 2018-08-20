package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_WRITE")')
class ApplicationController {
    ApplicationService applicationService

    def index(String teacherId) {
        println 'hh'
        renderJson applicationService.list(teacherId)
    }

    /**
     * 保存
     */
    def save(String teacherId) {
        def cmd = new ProjectCommand()
        bindData(cmd, request.JSON)
        cmd.principalId = teacherId
        def form = applicationService.create(cmd)
        renderJson([id: form.id])
    }

    def show(String teacherId, Long id) {
        renderJson applicationService.getFormInfo(id)
    }

    def edit(String teacherId, Long id) {
        renderJson applicationService.getFormForEdit(id)
    }

    /**
     * 创建
     */
    def create(String teacherId) {
        renderJson applicationService.getFormForCreate()
    }

    /**
     * 获取审核人
     */
    def checkers(String teacherId) {
        renderJson applicationService.checkers
    }

    def patch(String teacherId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                applicationService.submit(teacherId, cmd)
                break
        }
        renderOk()
    }

}
