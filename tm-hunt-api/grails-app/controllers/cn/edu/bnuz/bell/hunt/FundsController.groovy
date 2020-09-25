package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import cn.edu.bnuz.bell.hunt.cmd.FundCommand
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.method.P
import org.springframework.security.access.prepost.PreAuthorize

class FundsController implements ServiceExceptionHandler {
    FundService fundService
    CheckerService checkerService
    SecurityService securityService

    def index(String month, String fundType) {
        if (month != null && month != 'null') {
            renderJson([monthes: fundService.monthesCreated(), funds: fundService.list(month, fundType)])
        } else {
            renderJson([monthes: fundService.monthesCreated()])
        }
    }

    /**
     * 上传文件
     */
    @PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
    def importData(String fundType) {
        if (!fundType) {
            throw new BadRequestException('缺少参数！')
        }
        renderJson ([table: fundService.upload(fundType as FundType, request)])
    }

    def show(Long id) {
        Project project = Project.get(id)
        if (!project) {
            throw new BadRequestException('项目不存在！')
        }
        if (securityService.userId != project.principal.id
                && !(securityService.hasPermission("PERM_HUNT_CHECK") && checkerService.getDepartmentId(securityService.userId) == project.department.id)
                && !securityService.hasRole("ROLE_HUNT_ADMIN")) {
            throw new ForbiddenException()
        }
        renderJson([projectName: project.name, funds: fundService.getProjectFunds(project.id)])
    }

    /**
     * 保存数据
     */
    def save() {
        def cmd = new FundCommand()
        bindData(cmd, request.JSON)
        if (!cmd.table) {
            renderOk()
        } else {
            renderJson(fundService.create(cmd))
        }
    }
}
