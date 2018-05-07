package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher

class Project {
    /**
     * 负责人信息
     */
    Teacher principal
    String title
    String degree
    String email
    String discipline
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
    Origin  origin
    String members
    String content
    String achievements

    static hasOne = [
            application: Application,
            contract: Contract
    ]

    static hasMany = [review: Review]

    static mapping = {
        comment                 '项目'
        table                   schema: 'tm_hunt'
        id                      generator: 'identity', comment: '项目ID'
        name                    length: 50, comment: '项目名称'
        principal               comment: '项目负责人'
        title                   length: 20, comment: '职称'
        degree                  length: 20, comment: '学位'
        email                   length: 50, comment: '邮箱'
        discipline              length: 20, comment: '学科'
        direction               length: 20, comment: '方向'
        department              comment: '部门'
        office                  length: 20, comment: '行政岗位'
        phone                   length: 30, comment: '电话'
        urls                    length: 300, comment: '支撑网址'
        status                  sqlType: 'tm_hunt.status', type: StatusUserType, comment: '建设情况'
        subtype                 comment: '项目类别'
        origin                  comment: '项目来源'
        members                 length: 50, comment: '参与人'
        application             comment: '立项申请'
        content                 length: 1500, comment: '主要内容'
        achievements            length: 1500, comment: '预期成果'
    }
    static constraints = {
        direction       nullable: true
        office          nullable: true
        urls            nullable: true
        application     nullable: true
        content         nullable: true
        achievements    nullable: true
    }
}
