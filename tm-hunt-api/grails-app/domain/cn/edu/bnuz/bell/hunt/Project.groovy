package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher

import java.time.LocalDate

class Project {
    /**
     * 负责人信息
     */
    Teacher principal
    String title
    String degree
    String email
    String discipline
    String major
    String direction

    Department department
    String office
    String phone

    /**
     * 项目信息
     */
    String name
    String urls
    Status status
    Subtype subtype
    Level level
    Origin origin
    String members

    /**
     * 立项日期
     */
    LocalDate dateStart

    /**
     * 项目编号
     */
    String code

    /**
     * 预期中期年份
     */
    Integer middleYear

    /**
     * 预期结项年份
     */
    Integer knotYear

    /**
     * 延期次数
     */
    Integer delayTimes

    /**
     * 主要内容
     */
    String content

    /**
     * 预期成果
     */
    String achievements

    /**
     * 申报书
     */
    String mainInfoForm

    /**
     * 备注
     */
    String memo

    static hasOne = [contract: Contract]

    static hasMany = [review: Review, infoChange: InfoChange]

    static mapping = {
        comment                 '项目'
        table                   schema: 'tm_hunt'
        id                      generator: 'identity', comment: '项目ID'
        name                    type: 'text', comment: '项目名称'
        principal               comment: '项目负责人'
        title                   type: 'text', comment: '职称'
        degree                  type: 'text', comment: '学位'
        email                   type: 'text', comment: '邮箱'
        discipline              type: 'text', comment: '学科'
        major                   type: 'text', comment: '专业'
        direction               type: 'text', comment: '方向'
        department              type: 'text', comment: '部门'
        office                  type: 'text', comment: '行政岗位'
        phone                   type: 'text', comment: '电话'
        urls                    type: 'text', comment: '支撑网址'
        status                  sqlType: 'tm_hunt.status', type: StatusUserType, comment: '建设情况'
        level                   sqlType: 'tm_hunt.level', type: LevelUserType, comment: '项目等级'
        subtype                 comment: '项目类别'
        origin                  comment: '项目来源'
        members                 type: 'text', comment: '参与人'
        dateStart               comment: '立项日期'
        code                    type: 'text', comment: '项目编号'
        middleYear              type: 'text', comment: '预期中期年份'
        knotYear                type: 'text', comment: '预期结项年份'
        content                 type: 'text', comment: '主要内容'
        achievements            type: 'text', comment: '预期成果'
        delayTimes              defaultValue: 0, comment: '延期次数'
        mainInfoForm            type: 'text', comment: '申报书'
        memo                    type: 'text', comment: '备注'
    }
    static constraints = {
        direction       nullable: true
        office          nullable: true
        urls            nullable: true
        members         nullable: true
        contract        nullable: true
        dateStart       nullable: true
        middleYear      nullable: true
        knotYear        nullable: true
        content         nullable: true
        achievements    nullable: true
        code            nullable: true
        discipline      nullable: true
        major           nullable: true
        direction       nullable: true
        mainInfoForm    nullable: true
        memo            nullable: true
        title           nullable: true
        degree          nullable: true
        email           nullable: true
        phone           nullable: true
    }
}
