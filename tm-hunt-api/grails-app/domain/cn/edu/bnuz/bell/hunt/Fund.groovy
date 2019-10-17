package cn.edu.bnuz.bell.hunt

class Fund {
    /**
     * 项目
     */
    Project project

    /**
     * 经费来源
     */
    Level level

    /**
     * 阶段：1：立项，2：中期，3：结项
     */
    Integer reportType

    /**
     * 经费类型
     */
    FundType type

    Date dateCreated

    BigDecimal amount

    String memo

    static mapping = {
        comment '经费明细'
        table schema: 'tm_hunt'
        id generator: 'identity', comment: '经费ID'
        project comment: '项目'
        level comment: '经费来源行政级别'
        reportType comment: '阶段'
        type sqlType: 'tm_hunt.fund_type', type: FundUserType, comment: '经费类型'
        amount comment: '金额'
        memo type: 'text', comment: '备注'
    }

    static constraints = {
        memo nullable: true
        reportType(unique: ['project', 'level', 'type'])
    }
}
