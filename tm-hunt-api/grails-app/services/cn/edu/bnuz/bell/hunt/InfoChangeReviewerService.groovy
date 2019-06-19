package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.ReviewerProvider
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class InfoChangeReviewerService implements ReviewerProvider{
    SecurityService securityService

    List<Map> getReviewers(Object id, String activity) {
        switch (activity) {
            case Activities.CHECK:
                return getCheckers(id) + getApprovers()
            case Activities.APPROVE:
                return getApprovers()
            case Activities.REVIEW:
                return getReviewers() + getApprovers()
            default:
                throw new BadRequestException()
        }
    }

    List<Map> getCheckers(id) {
        Checker.executeQuery'''
select new map(teacher.id as id, teacher.name as name)
from Checker checker 
join checker.teacher teacher,
InfoChange infoChange
where checker.department = infoChange.department and infoChange.id = :id
''', [id: id]
    }

    List<Map> getApprovers() {
        User.findAllWithPermission('PERM_HUNT_ADMIN')
    }

    List<Map> getReviewers() {
        User.findAllWithPermission('PERM_HUNT_DIRECT')
    }
}
