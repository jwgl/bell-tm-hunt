package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.BadRequestException
import cn.edu.bnuz.bell.security.SecurityService
import cn.edu.bnuz.bell.security.User
import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.ReviewerProvider
import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class ProjectReviewerService implements ReviewerProvider{
    SecurityService securityService

    List<Map> getReviewers(Object id, String activity) {
        switch (activity) {
            case Activities.CHECK:
                return getCheckers()
            case Activities.APPROVE:
                return getApprovers()
            default:
                throw new BadRequestException()
        }
    }

    List<Map> getCheckers() {
        Checker.executeQuery'''
select new map(teacher.id as id, teacher.name as name)
from Checker checker 
join checker.teacher teacher
where checker.department.id = :id
''', [id: securityService.departmentId]
    }

    List<Map> getApprovers() {
        User.findAllWithPermission('PERM_HUNT_ADMIN')
    }
}
