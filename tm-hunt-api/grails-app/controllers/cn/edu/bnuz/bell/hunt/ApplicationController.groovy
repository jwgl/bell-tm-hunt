package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.multipart.MultipartFile

@PreAuthorize('hasAuthority("PERM_HUNT_WRITE")')
class ApplicationController {
    ApplicationService applicationService
    InspectFormService inspectFormService
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
        MultipartFile uploadFile = request.getFile('file')
        if (taskId && prefix && !uploadFile.empty) {
            def filePath = "${filesPath}/${taskId}/${teacherId}"
            def ext = uploadFile.originalFilename.substring(uploadFile.originalFilename.lastIndexOf('.') + 1).toLowerCase()
            def filename = "${prefix}_${UUID.randomUUID()}.${ext}"
            File dir= new File(filePath)
            if (!dir.exists() || dir.isFile()) {
                dir.mkdirs()
            }
            uploadFile.transferTo( new File(filePath, filename) )
            renderJson([file: filename])
        } else {
            throw new BadRequestException('Empty file.')
        }

    }

    /**
     * 下载附件
     * @param teacherId 负责人ID
     * @param applicationId 申请ID
     * @return
     */
    def attachments(String teacherId, Long applicationId) {
        def review = Review.load(applicationId)
        if (!review) {
            throw new NotFoundException()
        }
        if (review.project.principal.id != teacherId) {
            throw new ForbiddenException()
        }
        def basePath = "${filesPath}/${review.reviewTask.id}/${teacherId}"
        response.setHeader("Content-disposition",
                "attachment; filename=\"" + URLEncoder.encode("${review.project.subtype.name}-${review.project.name}-${review.project.principal.name}.zip", "UTF-8") + "\"")
        response.contentType = "application/zip"
        response.outputStream << ZipTools.zip(review, basePath)
        response.outputStream.flush()
    }

}
