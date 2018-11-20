package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.cmd.ApprovalOperationCommand
import cn.edu.bnuz.bell.hunt.cmd.BatCommand
import cn.edu.bnuz.bell.hunt.cmd.ExpertReviewCommand
import cn.edu.bnuz.bell.workflow.State
import grails.gorm.transactions.Transactional

@Transactional
class ApplicationAdministrationService {

    def lock(BatCommand cmd) {
        cmd.ids.each { id ->
            def form = Review.load(id)
            if (form) {
                form.locked = cmd.checked
                form.save()
            }
        }
    }

    def createExpertReview(BatCommand cmd) {
        def experts = Expert.findAllByTeam(cmd.teamNum)
        if (!experts || !experts.size()) {
            throw new BadRequestException()
        }
        cmd.ids.each { id ->
            def form = Review.load(id)
            if (form) {
                if (!ExpertReview.findByReview(form)) {
                    experts.each { expert ->
                        def expertReview = new ExpertReview(
                                review: form,
                                expert: expert.teacher
                        )
                        if (!expertReview.save()) {
                            expertReview.errors.each {
                                println it
                            }
                        }
                    }
                }
            }
        }
    }

    def removeExpert(Long id) {
        def form = Review.load(id)
        if (ExpertReview.findByReviewAndDateReviewedIsNotNull(form)) {
            return false
        }
        def expertReview = ExpertReview.findAllByReview(form)
        expertReview.each { item -> item.delete()}
        return true
    }

    def updateConclusion(Long id, ApprovalOperationCommand cmd) {
        def form = Review.load(id)
        if (!form || form.status != State.CHECKED) {
            throw new BadRequestException()
        }
        form.setConclusionOfUniversity(cmd.conclusionOfUniversity ? (cmd.conclusionOfUniversity as Conclusion) : null)
        form.setConclusionOfProvince(cmd.conclusionOfProvince ? (cmd.conclusionOfProvince as Conclusion) : null)
        form.setOpinionOfUniversity(cmd.opinionOfUniversity)
        form.setOpinionOfProvince(cmd.opinionOfProvince)
        form.save()
    }
}
