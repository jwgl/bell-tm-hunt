package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Teacher

/**
 * 评审专家
 */
class Expert {
    /**
     * 教师
     */
    Teacher teacher

    /**
     * 是否在聘期
     */
    Boolean enabled

    /**
     * 分组
     */
    Integer team

    static mapping = {
        comment         '评审专家'
        table           schema: 'tm_hunt'
        id              generator: 'identity', comment: '无意义ID'
        teacher         comment: '教师'
        enabled         comment: '是否在聘期'
        team            comment: '分组'
    }

    static constraints = {
        team    nullable: true
    }

}
