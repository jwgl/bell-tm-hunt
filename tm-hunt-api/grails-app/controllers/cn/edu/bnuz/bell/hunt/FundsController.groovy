package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ServiceExceptionHandler
import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class FundsController implements ServiceExceptionHandler {
    FundDataImportService fundDataImportService

    def index() { }

    /**
     * 上传文件
     */
    def importData(String fundType) {
        if (!fundType) {
            throw new BadRequestException('缺少参数！')
        }
        renderJson ([table: fundDataImportService.upload(fundType as FundType, request)])
    }
}
