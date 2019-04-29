package cn.edu.bnuz.bell.hunt.cmd

import cn.edu.bnuz.bell.hunt.Level
import cn.edu.bnuz.bell.hunt.Status
import grails.validation.Validateable

class ProjectDepartmentOptionCommand implements Validateable {
    String status
    List<Long> subtypeIds
    List<Integer> middleYears
    List<Integer> knotYears
    String level
    String code
    String departmentId
    String q

    Map getArgs() {
        def arg = [:]

        if (q) {
            arg += [q: "%${q}%"]
            return arg
        }
        if (departmentId) {
            arg += [departmentId: departmentId]
        }
        if (subtypeIds?.size() > 0) {
            arg += [subtypeIds: subtypeIds]
        }
        if (middleYears?.size()) {
            arg += [middleYears: middleYears]
        }
        if (knotYears?.size()) {
            arg += [knotYears: knotYears]
        }
        if (level) {
            arg += [level: level as Level]
        }
        if (status) {
            arg += [status: status as Status]
        }
        if (code) {
            arg += [code: code]
        }
        return arg
    }

    String getCriterion() {
        def criterion = ''

        if (q) {
            return '''
(project.department.name like :q 
or project.name like :q
or project.principal.name like :q
or project.code like :q
)'''
        }
        if (departmentId) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.department.id = :departmentId"
        }
        if (subtypeIds?.size() > 0) {
            criterion += "${criterion.isEmpty() ? "" : " and "}subtype.id in (:subtypeIds)"
        }
        if (middleYears?.size()) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.middleYear in (:middleYears)"
        }
        if (knotYears?.size()) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.knotYear in (:knotYears)"
        }
        if (level) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.level = :level"
        }
        if (status) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.status = :status"
        }
        if (code) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.code = :code"
        }
        return criterion
    }
}
