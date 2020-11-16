package cn.edu.bnuz.bell.hunt

/**
 * 评审专家
 */
class Expert {
    /**
     * 教师
     */
    String teacherId

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
        teacherId       length: 5, comment: '教师'
        enabled         comment: '是否在聘期'
        team            comment: '分组'
    }

    static constraints = {
        team    nullable: true
    }

}
