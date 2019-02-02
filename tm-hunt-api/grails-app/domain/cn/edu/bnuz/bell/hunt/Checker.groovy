package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher

class Checker {
    Department department
    Teacher teacher

    static mapping = {
        comment         '学院管理员'
        table           schema: 'tm_hunt'
        id              generator: 'identity', comment: '无意义ID'
        department      comment: '可管理部门'
        teacher         comment: '教师'
    }

    static constraints = {
        teacher         unique: 'department'
    }

}
