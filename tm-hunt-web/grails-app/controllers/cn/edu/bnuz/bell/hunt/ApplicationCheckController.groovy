package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize

@PreAuthorize('hasAuthority("PERM_HUNT_CHECK")')
class ApplicationCheckController {

    def index() { }
}
