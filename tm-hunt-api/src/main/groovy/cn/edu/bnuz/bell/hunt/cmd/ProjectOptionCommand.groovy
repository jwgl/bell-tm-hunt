package cn.edu.bnuz.bell.hunt.cmd

import cn.edu.bnuz.bell.hunt.Level
import grails.validation.Validateable

class ProjectOptionCommand implements Validateable {
    Integer reportType
    List<Long> subtypeIds
    List<Integer> middleYears
    List<Integer> knotYears
    String level
    List<String> departmentIds
    String code

    Map getArgs() {
        def arg = [:]

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
        if (departmentIds?.size()) {
            arg += [departmentIds: departmentIds]
        }
        if (code) {
            arg += [code: code]
        }
        if (reportType) {
            arg += [reportType: reportType]
        }
        return arg
    }

    String getCriterion() {
        def criterion = ''

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
        if (departmentIds?.size()) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.department.id in (:departmentIds)"
        }
        if (code) {
            criterion += "${criterion.isEmpty() ? "" : " and "}project.code = :code"
        }
        if (reportType) {
            def notExistStr = '''
not exists(
select r.id
from Review r
where r.project.id = project.id
and r.reportType = :reportType 
and ((
    r.status = 'FINISHED'
    and r.project.level = 'UNIVERSITY' and r.conclusionOfUniversity = 'OK'
    ) or (r.status = 'FINISHED'
    and r.project.level = 'PROVINCE' and r.conclusionOfProvince = 'OK'
) or (r.status <> 'FINISHED')))
'''
            criterion += "${criterion.isEmpty() ? "" : " and "} ${notExistStr} "
        }
        return criterion
    }
}
