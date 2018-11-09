package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ReviewTaskCommand
import cn.edu.bnuz.bell.security.SecurityService
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_IN_SCHOOL_TEACHER")')
class ReviewTaskController {
    ReviewTaskService reviewTaskService

    def index() {
        renderJson reviewTaskService.list()
    }

    /**
     * 保存数据
     */
    @PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
    def save() {
        def cmd = new ReviewTaskCommand()
        bindData(cmd, request.JSON)
        def form = reviewTaskService.create(cmd)
        renderJson([id: form.id])
    }

    def show(Long id) {
        renderJson reviewTaskService.getFormForShow(id)
    }

    /**
     * 编辑数据
     */
    @PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
    def edit(Long id) {
        renderJson reviewTaskService.getFormForShow(id)
    }

    /**
     * 更新数据
     */
    @PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
    def update(Long id) {
        def cmd = new ReviewTaskCommand()
        bindData(cmd, request.JSON)
        reviewTaskService.update(id, cmd)
        renderOk()
    }

}
