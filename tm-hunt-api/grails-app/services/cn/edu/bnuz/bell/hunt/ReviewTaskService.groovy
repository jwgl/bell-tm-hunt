package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ReviewTaskCommand
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.workflow.State
import com.sun.glass.ui.EventLoop
import grails.gorm.transactions.Transactional

@Transactional
class ReviewTaskService {
    SecurityService securityService

    def list() {
        ReviewTask.executeQuery'''
select new map(
    rt.id as id,
    rt.title as title,
    rt.startDate as startDate,
    rt.endDate as endDate,
    rt.type as type,
    rt.content as content,
    rt.ban as ban
)
from ReviewTask rt
order by rt.dateCreated desc
'''
    }

    def create(ReviewTaskCommand cmd) {
        def form = new ReviewTask(
                title: cmd.title,
                startDate: ReviewTaskCommand.toDate(cmd.startDate),
                endDate: ReviewTaskCommand.toDate(cmd.endDate),
                type: cmd.type as ReviewType,
                content: cmd.content,
                remind: cmd.remind,
                ban: cmd.ban as Level
        )
        if (!form.save()) {
           form.errors.each {
               println it
           }
        }
        return form
    }

    def getFormForShow(Long id) {
        def result = ReviewTask.executeQuery'''
select new map(
    rt.id as id,
    rt.title as title,
    rt.startDate as startDate,
    rt.endDate as endDate,
    rt.type as type,
    rt.content as content,
    rt.remind as remind,
    rt.ban as ban
)
from ReviewTask rt
where rt.id = :id
''', [id: id]
        if (result) {
            return result[0]
        }
    }

    def update(Long id, ReviewTaskCommand cmd) {
        def form = ReviewTask.load(id)
        if (form) {
            form.title = cmd.title
            form.startDate = ReviewTaskCommand.toDate(cmd.startDate)
            form.endDate = ReviewTaskCommand.toDate(cmd.endDate)
            form.type = cmd.type as ReviewType
            form.content = cmd.content
            form.remind = cmd.remind
            form.ban = cmd.ban as Level
            form.save()
        }
        return form
    }

    def listForApproval() {
        ReviewTask.executeQuery'''
select new map(
    rt.id as id,
    rt.title as title,
    rt.endDate as endDate,
    rt.type as type,
    sum (case when r.status != 'CREATED' then 1 else 0 end) as countProject,
    sum (case when r.status = 'CHECKED' then 1 else 0 end) as countUncheck,
    sum (case when r.status = 'FINISHED' and r.conclusionOfUniversity = 'OK' then 1 else 0 end) as countPass,
    sum (case when r.status = 'FINISHED' and r.conclusionOfUniversity = 'VETO' then 1 else 0 end) as countFail
)
from Review r
right join r.reviewTask rt
group by rt.id, rt.title, rt.endDate, rt.type
order by rt.dateCreated desc
'''
    }

    def listForDepartment() {
        ReviewTask.executeQuery'''
select new map(
    rt.id as id,
    rt.title as title,
    rt.endDate as endDate,
    rt.type as type,
    sum (case when r.status != 'CREATED' and r.department.id = :departmentId then 1 else 0 end) as countProject,
    sum (case when r.status = 'SUBMITTED' and r.department.id = :departmentId then 1 else 0 end) as countUncheck,
    sum (case when r.status in (:passStates) and r.department.id = :departmentId then 1 else 0 end) as countPass,
    sum (case when r.status in (:failStates) and r.department.id = :departmentId  then 1 else 0 end) as countFail,
    sum (case when r.status = 'FINISHED' and r.department.id = :departmentId then 1 else 0 end) as countFinal
)
from Review r
right join r.reviewTask rt
where (current_date between rt.startDate and rt.endDate) or r.id is not null
group by rt.id, rt.title, rt.endDate, rt.type
order by rt.dateCreated desc
''', [departmentId: securityService.departmentId,
      passStates: [State.APPROVED, State.CHECKED],
      failStates: [State.REJECTED, State.CLOSED]]
    }

    /**
     * @return 已申请过的任务和申请项目数、正在开放的任务
     */
    def listForTeacher() {
        ReviewTask.executeQuery'''
select new map(
    rt.id as id,
    rt.title as title,
    rt.endDate as endDate,
    rt.type as type,
    rt.ban as ban,
    sum (case when p.principal = :teacher then 1 else 0 end) as countApplication
)
from Review r
right join r.reviewTask rt
left join r.project p
where (current_date between rt.startDate and rt.endDate) or r.id is not null
group by rt.id, rt.title, rt.endDate, rt.type
order by rt.dateCreated desc
''', [teacher: Teacher.load(securityService.userId)]
    }
}
