package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ExpertReviewCommand
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.commands.SubmitCommand
import org.springframework.security.access.prepost.PreAuthorize


@PreAuthorize('hasRole("ROLE_HUNT_EXPERT")')
class ExpertReviewController {
    ExpertReviewService expertReviewService

    def index(String expertId, Long taskId, Integer reviewType, String type) {
        renderJson expertReviewService.list(taskId, reviewType, type)
    }

    def show(String expertId, Long id) {
        renderJson expertReviewService.getInfoForReview(id)
    }

    def update(String expertId, Long id) {
        def cmd = new ExpertReviewCommand()
        bindData(cmd, request.JSON)
        expertReviewService.update(id, cmd)
        renderOk()
    }

    def patch(String expertId, Long id, String op) {
        def operation = Event.valueOf(op)
        switch (operation) {
            case Event.SUBMIT:
                expertReviewService.submit(id)
                break
        }
        renderOk()
    }
}
