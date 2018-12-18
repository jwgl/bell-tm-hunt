package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Teacher

/**
 * 专家评审
 */
class ExpertReview {
    /**
     * 所属项目审核
     */
    Review review

    /**
     * 专家
     */
    Teacher expert

    /**
     * 专家意见
     */
    String opinion

    /**
     * 结论
     */
    String conclusion

    /**
     * 评分
     */
    Integer value

    /**
     * 评审日期
     */
    Date dateReviewed

    static belongsTo = [review: Review]

    static mapping = {
        comment                 '专家评审'
        table                   schema: 'tm_hunt'

        id                      generator: 'identity', comment: '无意义ID'
        expert                  comment: '专家'
        opinion                 length: 1500, comment: '专家意见'
        conclusion              length: 5, comment: '专家评审结论'
        review                  comment: '所属项目审核'
        value                   defaultValue: 0, comment: '评分'
    }
    static constraints = {
        opinion         nullable: true
        conclusion      nullable: true
        dateReviewed    nullable: true
    }
}
