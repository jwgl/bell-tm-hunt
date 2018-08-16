package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher

/**
 * 立项申请，信息冗余，在后面项目信息变更后能保持立项申请的原始信息
 */
class Application {
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
    String members

//    static belongsTo = [application: Project]

    static mapping = {
        comment                 '立项申请'
        table                   schema: 'tm_hunt'
        id                      generator: 'identity', comment: '项目申请ID'
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
        members                 length: 50, comment: '参与人'
//        application                 comment: '所属项目'
    }
    static constraints = {
        direction   nullable: true
        office      nullable: true
        urls        nullable: true
    }
}
