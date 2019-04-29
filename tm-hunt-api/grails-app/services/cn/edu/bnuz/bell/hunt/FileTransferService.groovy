package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

@Transactional
class FileTransferService {
    @Value('${bell.teacher.filesPath}')
    String filesPath

    def upload(String prefix, String base, HttpServletRequest request) {
        MultipartFile uploadFile = request.getFile('file')
        if (prefix && !uploadFile.empty) {
            def filePath = "${filesPath}/${base}"
            def ext = uploadFile.originalFilename.substring(uploadFile.originalFilename.lastIndexOf('.') + 1).toLowerCase()
            def filename = "${prefix}_${UUID.randomUUID()}.${ext}"
            File dir= new File(filePath)
            if (!dir.exists() || dir.isFile()) {
                dir.mkdirs()
            }
            uploadFile.transferTo( new File(filePath, filename) )
            return filename
        } else {
            throw new BadRequestException('Empty file.')
        }
    }

    def uploadKeepFileName(String base, HttpServletRequest request) {
        MultipartFile uploadFile = request.getFile('file')
        if (!uploadFile.empty) {
            def filePath = "${filesPath}/${base}"
            def filename = uploadFile.originalFilename
            File dir= new File(filePath)
            if (!dir.exists() || dir.isFile()) {
                dir.mkdirs()
            }
            uploadFile.transferTo( new File(filePath, filename) )
            return filename
        } else {
            throw new BadRequestException('Empty file.')
        }
    }

    def download(Object form, HttpServletResponse response) {
        if (form instanceof ReviewTask) {
            def basePath = "${filesPath}/review-task"
            def file = new File(basePath, form.attach)
            byte[] bytes = file.bytes
            response.setHeader("Content-disposition",
                    "attachment; filename=\"" + URLEncoder.encode("${form.attach}", "UTF-8") + "\"")
            response.contentType = URLConnection.guessContentTypeFromName(file.getName())
            response.outputStream << bytes
        } else if (form instanceof Review) {
            def basePath = "${filesPath}/${form.project.principal.id}"
            response.setHeader("Content-disposition",
                    "attachment; filename=\"" + URLEncoder.encode("${form.project.subtype.name}-${form.project.name}-${form.project.principal.name}.zip", "UTF-8") + "\"")
            response.contentType = "application/zip"
            response.outputStream << ZipTools.zip(form, basePath)
        } else if (form instanceof InfoChange) {
            def basePath = "${filesPath}/info-change/${form.applicant.id}"
            response.setHeader("Content-disposition",
                    "attachment; filename=\"" + URLEncoder.encode("${form.project.subtype.name}-${form.project.name}-${form.project.principal.name}.zip", "UTF-8") + "\"")
            response.contentType = "application/zip"
            response.outputStream << ZipTools.zip(form, basePath)
        }
        response.outputStream.flush()
    }
}
