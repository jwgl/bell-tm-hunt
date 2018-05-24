package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ReviewTaskCommand
import grails.gorm.transactions.Transactional

@Transactional
class ReviewTaskService {

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
order by rt.dateCreated 
'''
    }

    def listForPerson() {
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
where rt.type = :type
order by rt.dateCreated desc
''', [type: ReviewType.APPLICATION]
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
}
