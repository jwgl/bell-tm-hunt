package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.cmd.DownloadCommand
import cn.edu.bnuz.bell.hunt.utils.ZipTools
import cn.edu.bnuz.bell.security.SecurityService
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import java.time.LocalDate

@Transactional
class FileTransferService {
    @Value('${bell.teacher.filesPath}')
    String filesPath
    SecurityService securityService

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
             response.setHeader("Content-disposition",
                    "attachment; filename=\"" + URLEncoder.encode("${form.title}.zip", "UTF-8") + "\"")
            response.contentType = "application/zip"
            response.outputStream << ZipTools.zip(form, basePath)
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

    def downloadAll(DownloadCommand cmd, HttpServletResponse response) {
        List<Review> reviews = []
        switch (cmd.role) {
            case 'ADMIN':
                reviews = Review.executeQuery'''
select r
from Review r
where r.id in (:ids)
''', [ids: cmd.ids]
                break
            case 'EXPERT':
                reviews = Review.executeQuery'''
select r
from Review r
join r.expertReview er
join er.expert e
where r.id in (:ids) and e.id = :expert
''', [ids: cmd.ids, expert: securityService.userId]
        }
        response.setHeader("Content-disposition",
                "attachment; filename=\"" + URLEncoder.encode("项目附件_${LocalDate.now().toString()}.zip", "UTF-8") + "\"")
        response.contentType = "application/zip"
        response.outputStream << ZipTools.zipAll(reviews, filesPath)
        response.outputStream.flush()
    }
}
