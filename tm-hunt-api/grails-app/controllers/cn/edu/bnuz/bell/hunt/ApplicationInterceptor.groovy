package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus


class ApplicationInterceptor {
    SecurityService securityService

    boolean before() {
        if (params.teacherId != securityService.userId) {
            render(status: HttpStatus.FORBIDDEN)
            return false
        } else {
            return true
        }
    }

    boolean after() { true }

    void afterView() {}
}
