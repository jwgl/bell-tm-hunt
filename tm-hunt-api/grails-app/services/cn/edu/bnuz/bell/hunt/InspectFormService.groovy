package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.hunt.cmd.ProjectCommand
import grails.gorm.transactions.Transactional

@Transactional
class InspectFormService {

    def getFormForEdit(Long id) {
        def result = Review.executeQuery '''
select new map(
    application.id as id,
    project.name as name,
    project.phone as phone,
    application.reportType as reportType,
    application.status as status,
    application.content as content,
    application.further as further,
    application.other as other,
    application.mainInfoForm as mainInfoForm,
    application.proofFile as proofFile,
    application.summaryReport as summaryReport
)
from Review application
join application.project project
where application.id = :id
''', [id: id]
        return result ? result[0] : null
    }

    def update(Long id, ProjectCommand cmd) {
        def review = Review.load(id)
        if (!review) {
            throw new BadRequestException()
        }

        def form = review.project
        form.setPhone(cmd.phone)

        review.setContent(cmd.content)
        review.setFurther(cmd.achievements)
        review.setOther(cmd.other)
        review.setMainInfoForm(cmd.mainInfoForm)
        review.setProofFile(cmd.proofFile)
        review.setSummaryReport(cmd.summaryReport)
        form.save()
    }
}
