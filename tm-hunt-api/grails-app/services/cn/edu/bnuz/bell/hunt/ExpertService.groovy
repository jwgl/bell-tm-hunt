package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ExpertCommand
import grails.gorm.transactions.Transactional

@Transactional
class ExpertService {

    /**
     * 专家列表
     */
    def list() {
        Expert.executeQuery'''
select new map(
    e.id as id,
    e.teacherId as teacherId,
    u.name as teacherName,
    (select sex from Teacher where id = e.teacherId) as sex,
    (select academicTitle from Teacher where id = e.teacherId) as academicTitle,
    (select academicDegree from Teacher where id = e.teacherId) as academicDegree,
    u.email as email,
    u.longPhone as phone,
    (select d.name from Teacher t join t.department d where t.id = e.teacherId) as departmentName
)
from Expert e, User u
where e.teacherId = u.id and e.team is null
'''
    }

    /**
     * 保存
     */
    def create(ExpertCommand cmd) {
        Expert expert = new Expert(
                teacherId: cmd.teacherId,
                enabled: true
        )
        expert.save()
        return expert
    }

    /**
     * 编辑
     */
    def getFormForEdit(Long id) {
        Expert.executeQuery'''
select new map(
    e.id as id,
    e.enabled as enabled,
    e.teacherId as teacherId,
    u.name as teacherName
)
from Expert e, User u 
where e.teacherId = u.id and e.id = :id
''', [id: id]
    }

    /**
     * 更新enabled
     */
    def update(Long id, Boolean enabled) {
        Expert expert = Expert.load(id)
        expert.setEnabled(enabled)
        expert.save()
    }

    /**
     * 建立分组
     */
    def makeTeam(ids) {
        def result = Expert.executeQuery'''select distinct team from Expert'''
        for (int i = 1; i < 100; i++) {
            if (!(i in result)) {
                ids.each { id ->
                    def expert = Expert.load(id)
                    if (expert) {
                        expert.setTeam(i)
                        expert.save()
                    }
                }
                break
            }
        }
    }

    /**
     * 载入分组
     */
    def loadTeam() {
        Expert.executeQuery'''
select new map(
    u.name as teacherName,
    e.team as team
)
from Expert e, User u
where e.teacherId = u.id and e.team is not null
'''
    }

    /**
     * 解散分组
     */
    def drop(Integer team) {
        Expert.executeUpdate'''update Expert set team = null where team = :team''', [team: team]
    }
}
