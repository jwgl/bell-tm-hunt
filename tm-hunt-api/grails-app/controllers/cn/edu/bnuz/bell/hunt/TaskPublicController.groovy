package cn.edu.bnuz.bell.hunt

import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_IN_SCHOOL_TEACHER")')
class TaskPublicController {
    ReviewTaskService reviewTaskService
    ApplicationService applicationService

    def index(String teacherId) {
        renderJson reviewTaskService.listForTeacher()
    }

    def show(String teacherId, Long id) {
        renderJson ([task: reviewTaskService.getFormForShow(id), applications: applicationService.list(teacherId, id)])
    }
}
