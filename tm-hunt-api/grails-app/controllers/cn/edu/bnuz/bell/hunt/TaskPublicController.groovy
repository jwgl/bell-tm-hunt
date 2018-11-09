package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_IN_SCHOOL_TEACHER")')
class TaskPublicController {
    ReviewTaskService reviewTaskService

    def index() {
        renderJson reviewTaskService.listForTeacher()
    }
}
