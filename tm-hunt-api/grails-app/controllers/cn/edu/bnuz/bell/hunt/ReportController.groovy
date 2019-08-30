package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.report.ReportClientService
import cn.edu.bnuz.bell.report.ReportRequest
import cn.edu.bnuz.bell.security.SecurityService


class ReportController {
    ReportClientService reportClientService
    SecurityService securityService

    def show(Integer id, String type) {
        def reportName = "hunt-${type}"
        println id
        def parameters
        def format
        switch (type) {
            case 'application-department':
                if (!securityService.hasRole('ROLE_HUNT_CHECKER')) {
                   throw new ForbiddenException()
                }
                format = 'xlsx'
                parameters = [task_id: id, user_id: securityService.userId]
                break
            case 'application':
                if (!securityService.hasRole('ROLE_HUNT_ADMIN')) {
                    throw new ForbiddenException()
                }
                format = 'xlsx'
                parameters = [task_id: id]
                break
            default:
                throw new BadRequestException()
        }
        def reportRequest = new ReportRequest(
                reportName: reportName,
                format: format,
                parameters: parameters
        )
        reportClientService.runAndRender(reportRequest, response)
    }
}
