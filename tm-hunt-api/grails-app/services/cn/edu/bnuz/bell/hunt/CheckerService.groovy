package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.CheckerCommand
import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher
import grails.gorm.transactions.Transactional

@Transactional
class CheckerService {

    /**
     * 审核人列表
     */
    def list() {
        Checker.executeQuery'''
select new map(
    c.id as id,
    t.id as teacherId,
    t.name as teacherName,
    d.name as departmentName
)
from Checker c
join c.teacher t
join c.department d
'''
    }

    /**
     * 保存
     */
    def create(CheckerCommand cmd) {
        Checker checker = new Checker(
                teacher: Teacher.load(cmd.teacherId),
                department: Department.load(cmd.departmentId)
        )
        checker.save()
        return checker
    }

    /**
     * 编辑
     */
    def getFormForEdit(Long id) {
        Checker.executeQuery'''
select new map(
    c.id as id,
    c.department.id as departmentId,
    t.id as teacherId,
    t.name as teacherName
)
from Checker c 
join c.teacher t
where c.id = :id
''', [id: id]
    }

    /**
     * 删除
     */
    def delete(Long id) {
        def form = Checker.get(id)
        if (form) {
            form.delete()
        }
    }

    def getDepartmentId(String teacherId) {
        def result = Checker.executeQuery'''
select c.department.id as departmentId from Checker c join c.teacher t where t.id = :id
''', [id: teacherId]
        return result ? result[0] : null
    }
}
