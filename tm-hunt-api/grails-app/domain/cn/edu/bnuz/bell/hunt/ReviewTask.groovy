package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.orm.PostgreSQLStringArrayUserType

import java.time.LocalDate

/**
 * 项目审核安排
 */
class ReviewTask {
    /**
     * 通知标题
     */
    String title

    /**
     * 通知内容
     */
    String content

    /**
     * 开始日期
     */
    LocalDate startDate

    /**
     * 截止日期
     */
    LocalDate endDate

    /**
     * 类型： APPLICATION（申报审核）、CHECK（检查审核）、OTHER（其他）
     */
    ReviewType type

    /**
     * 专家注意事项
     */
    String remind

    /**
     * 创建日期
     */
    Date dateCreated

    /**
     * 禁止有该等级在研项目的对象申请新项目
     */
    Level ban

    /**
     * 附件
     */
    String[] attach


    static mapping = {
        comment         '项目审核安排'
        table           schema: 'tm_hunt'
        id              generator: 'identity', comment: '安排ID'
        title           comment: '通知标题'
        content         type: 'text', comment: '通知内容'
        startDate       comment: '开始日期'
        endDate         comment: '截止日期'
        type            sqlType: 'tm_hunt.review_type', type: ReviewTypeUserType, comment: '检查类型'
        remind          type: 'text', comment: '专家注意事项'
        dateCreated     comment: '创建日期'
        ban             sqlType: 'tm_hunt.level', type: LevelUserType, comment: '禁止对象'
        attach          sqlType: 'text[]', type: PostgreSQLStringArrayUserType, comment: '附件'
    }
    static constraints = {
        ban     nullable: true
        remind  nullable: true
        attach  nullable: true
    }

    Boolean isValidDate() {
        def now = LocalDate.now()
        return now.isAfter(startDate) && now.isBefore(endDate)
    }
}
