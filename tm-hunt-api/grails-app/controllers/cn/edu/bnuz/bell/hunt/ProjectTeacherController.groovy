package cn.edu.bnuz.bell.hunt


class ProjectTeacherController {
    ProjectTeacherService projectTeacherService
    def index() {
        renderJson(projectTeacherService.list())
    }

    def show(Long id) {
        renderJson(projectTeacherService.getFormInfo(id))
    }
}
