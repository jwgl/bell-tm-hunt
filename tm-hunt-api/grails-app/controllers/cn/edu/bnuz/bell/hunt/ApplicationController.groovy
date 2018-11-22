package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.multipart.MultipartFile

@PreAuthorize('hasAuthority("PERM_HUNT_WRITE")')
class ApplicationController {
    ApplicationService applicationService
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def index(String teacherId) {
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

    def update(String teacherId, Long id) {
        def cmd = new ProjectCommand()
        bindData(cmd, request.JSON)
        applicationService.update(id, cmd)
        renderOk()
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

    /**
     * 上传文件
     */
    def upload(String teacherId, Long taskId) {
        def prefix = params.prefix
        MultipartFile uploadFile = request.getFile('file')
        if (prefix && !uploadFile.empty) {
            def ext = uploadFile.originalFilename.substring(uploadFile.originalFilename.lastIndexOf('.') + 1).toLowerCase()
            def filePath = "${filesPath}/${taskId}/${teacherId}"
            def filename = "${filePath}/${prefix}_${UUID.randomUUID()}.${ext}"
            File dir= new File(filePath)
            if (!dir.exists() || dir.isFile()) {
                dir.mkdirs()
            } else {
                uploadFile.transferTo( new File(filename) )
            }
            renderJson([file: filename])
        } else {
            throw new BadRequestException('Empty file.')
        }

    }

}
