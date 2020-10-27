package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.InfoChangeCommand
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_WRITE")')
class InfoChangeController {
    InfoChangeService infoChangeService
    FileTransferService fileTransferService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String teacherId) {
        renderJson infoChangeService.list(teacherId)
    }

    def show(String teacherId, Long id) {
        def form = infoChangeService.getInfoForShow(id)
        def project = infoChangeService.findProject(form?.projectId)
        infoChangeService.projectUpdatedBefore(id, project as Map)
        renderJson([
                form: form,
                project: project
        ])
    }

    def edit(String teacherId, Long id) {
        def form = infoChangeService.getInfoForShow(id)
        if (!form) {
            throw new NotFoundException()
        } else {
            renderJson([
                    form: form,
                    project: infoChangeService.findProject(form.projectId)
            ])
        }
    }

    def update(String teacherId, Long id) {
        def cmd = new InfoChangeCommand()
        bindData(cmd, request.JSON)
        infoChangeService.update(id, cmd)
        renderOk()
    }

    /**
     * 保存
     */
    def save(String teacherId) {
        def cmd = new InfoChangeCommand()
        bindData(cmd, request.JSON)
        def form = infoChangeService.create(cmd)
        renderJson([id: form.id])
    }

    def create(String teacherId) {
        renderJson infoChangeService.getFormForCreate(teacherId)
    }

    /**
     * 删除
     */
    def delete(Long id) {
        infoChangeService.delete(id)
        renderOk()
    }

    /**
     * 获取审核人
     */
    def checkers(String teacherId, Long infoChangeId) {
        renderJson infoChangeService.getCheckers(infoChangeId)
    }

    def findProject(String teacherId, Long id) {
        renderJson infoChangeService.findProject(id)
    }

    def patch(String teacherId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                def cmd = new SubmitCommand()
                bindData(cmd, request.JSON)
                cmd.id = id
                infoChangeService.submit(teacherId, cmd)
                break
        }
        renderOk()
    }

    /**
     * 上传文件
     */
    def upload(String teacherId) {
        def prefix = params.prefix
        renderJson ([file: fileTransferService.upload(prefix, "info-change/${teacherId}", request)])
    }
}
