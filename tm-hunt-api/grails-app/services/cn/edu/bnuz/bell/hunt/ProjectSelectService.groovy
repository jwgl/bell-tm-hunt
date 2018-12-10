package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.hunt.cmd.ProjectOptionCommand
import grails.gorm.transactions.Transactional

@Transactional
class ProjectSelectService {

    def list(ProjectOptionCommand cmd) {
        def sqlStr = '''
select new map(
    project.id as id,
    project.name as name,
    project.principal.name as principalName,
    project.level as level,
    subtype.name as subtype,
    origin.name as origin,
    project.code as code,
    project.middleYear as middleYear,
    project.knotYear as knotYear,
    project.dateStart as dateStart,
    department.name as departmentName
)
from Project project
join project.subtype subtype
join project.origin origin
join project.department department
where project.status = 'INHAND'
'''
        if (!cmd.criterion.isEmpty()) {
            sqlStr += " and ${cmd.criterion}"
        }
        sqlStr += " order by project.level, subtype.name, project.code"
        Project.executeQuery sqlStr, cmd.args
    }

    def getMiddleYears() {
        Project.executeQuery'''
select distinct new map(middleYear as middleYear)
from Project
where status = 'INHAND' and middleYear is not null
order by middleYear
'''
    }

    def getKnotYears() {
        Project.executeQuery'''
select distinct new map(knotYear as knotYear)
from Project
where status = 'INHAND'
order by knotYear
'''
    }
}
