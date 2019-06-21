package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.hunt.cmd.DownloadCommand
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService


class AttachmentController {
    FileTransferService fileTransferService
    SecurityService securityService

    def index(DownloadCommand cmd) {
        if ((cmd.role == 'ADMIN' && !securityService.hasRole('ROLE_HUNT_ADMIN'))
            || (cmd.role == 'EXPERT' && !securityService.hasRole('ROLE_HUNT_EXPERT'))
            || (cmd.role == 'CHECKER' && !securityService.hasRole('ROLE_HUNT_CHECKER'))
        ) {
            throw new ForbiddenException()
        }
        fileTransferService.downloadAll(cmd, response)
    }

    def show(Long id, String type) {
        def obj
        switch (type) {
            case 'REVIEW':
                obj = Review.load(id)
                break
            case 'INFO-CHANGE':
                obj = InfoChange.load(id)
                break
            case 'TASK':
                obj = ReviewTask.load(id)
                break
            default:
                throw new BadRequestException()
        }
        if (obj) {
            // 检查权限:管理员、处长和专家可以任意下载，学院审核员只能下载本学院的附件，个人只能下载自己的附件
            if (obj.instanceOf(ReviewTask)
                    || securityService.hasRole('ROLE_HUNT_ADMIN')
                    || securityService.hasRole('ROLE_HUNT_DIRECTOR')
                    || securityService.hasRole('ROLE_HUNT_EXPERT')
                    || (securityService.hasRole('ROLE_HUNT_CHECKER')
                        && Checker.findByTeacherAndDepartment(Teacher.load(securityService.userId), obj.department))
                    || obj.project.principal.id == securityService.userId
            ){
                fileTransferService.download(obj, response)
            }

        } else {
            throw new NotFoundException()
        }
    }
}
