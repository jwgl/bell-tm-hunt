package cn.edu.bnuz.bell.hunt

import java.time.LocalDate

class Contract {
    /**
     * 所属项目
     */
    Project project

    /**
     * 立项日期
     */
    LocalDate dateCreated

    /**
     * 计划中期检查年份
     */
    String yearForMid

    /**
     * 计划结项验收年份
     */
    String yearForEnding

    static belongsTo = [project: Project]

    static mapping = {
        comment         '项目合同'
        table           schema: 'tm_hunt'

        project         comment: '所属项目'
        dateCreated     comment: '立项时间'
        yearForMid      type: 'text', comment: '计划中期检查年份'
        yearForEnding   type: 'text', comment: '计划结项验收年份'
    }

    static constraints = {
        yearForMid      nullable: true
    }
}
