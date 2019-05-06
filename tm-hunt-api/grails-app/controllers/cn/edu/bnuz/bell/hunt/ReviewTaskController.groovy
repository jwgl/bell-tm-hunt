package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ReviewTaskCommand
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_ADMIN")')
class ReviewTaskController {
    ReviewTaskService reviewTaskService
    FileTransferService fileTransferService

    def index() {
        renderJson reviewTaskService.list()
    }

    /**
     * 保存数据
     */
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
    def edit(Long id) {
        renderJson reviewTaskService.getFormForShow(id)
    }

    /**
     * 更新数据
     */
    def update(Long id) {
        def cmd = new ReviewTaskCommand()
        bindData(cmd, request.JSON)
        reviewTaskService.update(id, cmd)
        renderOk()
    }

    /**
     * 上传文件
     */
    def upload() {
        renderJson ([file: fileTransferService.uploadKeepFileName("review-task", request)])
    }

}
