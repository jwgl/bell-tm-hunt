package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_WRITE")')
class ApplicationController {
    ApplicationService applicationService
    InspectFormService inspectFormService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String teacherId, Long taskId) {
        renderJson applicationService.list(teacherId, taskId)
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

    def update(String teacherId, Long id) {
        def cmd = new ProjectCommand()
        bindData(cmd, request.JSON)
        def review = Review.load(id)
        if ( review.reportType == 1) {
            applicationService.update(id, cmd)
        } else {
            inspectFormService.update(id, cmd)
        }
        renderOk()
    }

    def show(String teacherId, Long id) {
        renderJson applicationService.getFormInfo(id)
    }

    def edit(String teacherId, Long id) {
        def review = Review.load(id)
        if ( review.reportType == 1) {
            renderJson applicationService.getFormForEdit(id)
        } else {
            renderJson inspectFormService.getFormForEdit(id)
        }
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
    def checkers(String teacherId, Long applicationId) {
        renderJson applicationService.getCheckers(applicationId)
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

    /**
    * 删除
    */
    def delete(Long id) {
        applicationService.delete(id)
        renderOk()
    }

    /**
     * 上传文件
     */
    def upload(String teacherId, Long taskId) {
        def prefix = params.prefix
        renderJson ([file: fileTransferService.upload(prefix, "${teacherId}", request)])
    }
}
