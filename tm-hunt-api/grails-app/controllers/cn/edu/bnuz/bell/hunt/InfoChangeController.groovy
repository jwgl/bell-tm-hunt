package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.InfoChangeCommand
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.multipart.MultipartFile

@PreAuthorize('hasAuthority("PERM_HUNT_WRITE")')
class InfoChangeController {
    InfoChangeService infoChangeService
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
        MultipartFile uploadFile = request.getFile('file')
        if (prefix && !uploadFile.empty) {
            def filePath = "${filesPath}/info-change/${teacherId}"
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
    def attachments(String teacherId, Long infoChangeId) {
        def infoChange = InfoChange.load(infoChangeId)
        if (!infoChange) {
            throw new NotFoundException()
        }
        if (infoChange.project.principal.id != teacherId) {
            throw new ForbiddenException()
        }
        def basePath = "${filesPath}/info-change/${teacherId}"
        response.setHeader("Content-disposition",
                "attachment; filename=\"" + URLEncoder.encode("${infoChange.project.subtype.name}-${infoChange.project.name}-${infoChange.project.principal.name}.zip", "UTF-8") + "\"")
        response.contentType = "application/zip"
        response.outputStream << ZipTools.zip(infoChange, basePath)
        response.outputStream.flush()
    }
}
