package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.hunt.cmd.ExpertReviewCommand
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.service.DataAccessService
import cn.edu.bnuz.bell.workflow.State
import grails.gorm.transactions.Transactional

@Transactional
class ExpertReviewService {
    SecurityService securityService
    DataAccessService dataAccessService
    ApplicationService applicationService

    def list(Long taskId, Integer reviewType, type) {
        def sql = '''
select new map(
    application.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    application.dateSubmitted as date,
    project.title as title,
    project.degree as degree,
    department.name as departmentName,
    e.conclusion as conclusion,
    e.dateReviewed as dateReviewed
)
from ExpertReview e 
join e.review application
join application.project project
join project.subtype subtype
join project.origin origin
join application.department department
where e.expert = :user
and application.reviewTask.id = :taskId
and application.reportType in (:reportTypes)
and e.dateReviewed is null
order by application.dateChecked
'''
        def expert = Teacher.load(securityService.userId)
        if (type == 'done') {
            sql = sql.replace('e.dateReviewed is null', 'e.dateReviewed is not null')
        }

        def reportTypes = reportTypes(taskId, reviewType)
        def list = ExpertReview.executeQuery sql, [user: expert, taskId: taskId, reportTypes: reportTypes]

        return [
            list: list,
            counts: [
                todo: countTodo(taskId, reportTypes),
                done: countDone(taskId, reportTypes)
            ]
        ]
    }

    private static reportTypes (Long taskId, Integer reviewType) {
        def task = ReviewTask.load(taskId)
        if (task.type == ReviewType.APPLICATION) {
            return [1]
        }
        switch (reviewType) {
            case 0: return [2, 3]
            case 1: return [4]
        }
        return [0]
    }

    def countTodo(Long taskId, List<Integer> reportTypes) {
        dataAccessService.getLong('''
select count(*)
from ExpertReview e 
join e.review application
where e.expert.id = :userId
and application.reviewTask.id = :taskId
and application.reportType in (:reportTypes)
and e.dateReviewed is null
''', [userId: securityService.userId, taskId: taskId, reportTypes: reportTypes])
    }

    def countDone(Long taskId, List<Integer> reportTypes) {
        dataAccessService.getLong('''
select count(*)
from ExpertReview e 
join e.review application
where e.expert.id = :userId
and application.reviewTask.id = :taskId
and application.reportType in (:reportTypes)
and e.dateReviewed is not null
''', [userId: securityService.userId, taskId: taskId, reportTypes: reportTypes])
    }

    def getInfoForReview(Long id) {
        def application = Review.load(id)
        def expertReview = ExpertReview.findByExpertAndReview(Teacher.load(securityService.userId), application)
        if (!expertReview) {
            throw new ForbiddenException()
        }
        return [
                form: applicationService.getFormInfo(id),
                remind: application.reviewTask.remind,
                name: application.project.name,
                departmentOpinion: application.departmentOpinion,
                departmentConclusion: application.departmentConclusion,
                conclusion: expertReview.conclusion,
                editAble: !expertReview.dateReviewed,
                opinion: expertReview.opinion,
                score: expertReview.value
        ]
    }

    def update(Long id, ExpertReviewCommand cmd) {
        def application = Review.load(id)
        def expertReview = ExpertReview.findByExpertAndReview(Teacher.load(securityService.userId), application)
        if (!expertReview) {
            throw new ForbiddenException()
        }
        expertReview.setConclusion(cmd.conclusion)
        expertReview.setOpinion(cmd.opinion)
        expertReview.setValue(cmd.score)
        expertReview.save()
        if (cmd.isCommit) {
            submit(id)
        }
    }

    def submit(Long id) {
        def application = Review.load(id)
        def expertReview = ExpertReview.findByExpertAndReview(Teacher.load(securityService.userId), application)
        if (!expertReview) {
            throw new ForbiddenException()
        }
        if (!expertReview.dateReviewed) {
            expertReview.setDateReviewed(new Date())
            expertReview.save()
        }
    }
}
