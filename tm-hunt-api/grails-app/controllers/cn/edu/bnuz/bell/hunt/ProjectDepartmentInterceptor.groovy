package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.http.HttpStatus


class ProjectDepartmentInterceptor {
    SecurityService securityService

    boolean before() {
        if (params.checkerId != securityService.userId) {
            render(status: HttpStatus.FORBIDDEN)
            return false
        } else {
            return true
        }
    }

    boolean after() { true }

    void afterView() {}
}
