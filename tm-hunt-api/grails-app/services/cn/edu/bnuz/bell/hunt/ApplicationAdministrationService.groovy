package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.cmd.ApprovalOperationCommand
import cn.edu.bnuz.bell.hunt.cmd.BatCommand
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
                                value: 0,
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
        // 只有立项申报的时候需要设置一下内容
        if (form.reportType == 1) {
            if ((form.project.level == Level.PROVINCE && form.conclusionOfProvince == Conclusion.OK) ||
                    (form.project.level == Level.UNIVERSITY && form.conclusionOfUniversity == Conclusion.OK))   {

                form.project.setCode(cmd.code)
                form.project.setDateStart(ApprovalOperationCommand.toDate(cmd.dateStarted))
                form.project.setMiddleYear(cmd.middleYear)
                form.project.setKnotYear(cmd.knotYear)
                form.project.setContent(form.content)
                form.project.setAchievements(form.further)
                form.project.setMainInfoForm(form.mainInfoForm)
            } else {
                form.project.setCode(null)
                form.project.setDateStart(null)
                form.project.setMiddleYear(null)
                form.project.setKnotYear(null)
            }
        }
        form.project.save()
        form.save()
    }

    def expertReviews(Long reviewId) {
        ExpertReview.executeQuery'''
select new map(
    expert.name as name,
    expertReview.opinion as opinion
)
from ExpertReview expertReview
join expertReview.expert expert
where expertReview.review.id = :id and expertReview.dateReviewed is not null
''', [id: reviewId]
    }
}
