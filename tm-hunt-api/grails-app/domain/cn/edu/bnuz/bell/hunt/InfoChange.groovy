package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateObject
import cn.edu.bnuz.bell.workflow.StateUserType
import cn.edu.bnuz.bell.orm.PostgreSQLIntegerArrayUserType
import cn.edu.bnuz.bell.workflow.WorkflowInstance

import java.time.LocalDate

class InfoChange implements StateObject {
    /**
     * 申请人
     */
    Teacher applicant

    /**
     * 关联项目
     */
    Project project

    /**
     * 变更内容
     */
    Integer[] type

    /**
     * 变更原因
     */
    String reason

    /**
     * 变更申请明细
     */
    Teacher principal
    String title
    String degree
    String email
    String office
    String phone
    Integer middleYear
    Integer knotYear
    String name
    String content
    String achievements
    String members
    String other

    /**
     *申报书
     */
    String mainInfoForm

    /**
     * 审核相关
     */
    Department department
    LocalDate dateCreated
    Date dateSubmitted
    String departmentOpinion
    Teacher checker
    Date dateChecked
    Teacher approver
    String opinionOfUniversity
    Date dateApproved
    State status
    Teacher reviewer
    Date dateReviewed

    /**
     * 工作流实例
     */
    WorkflowInstance workflowInstance

    static hasMany = [items: ChangeItem]

    static mapping = {
        comment                 '项目变更审核'
        table                   schema: 'tm_hunt'
        id                      generator: 'identity', comment: '审核ID'
        applicant               comment: '申请人'
        project                 comment: '关联项目'
        type                    type: PostgreSQLIntegerArrayUserType, comment: '变更内容'
        principal               comment: '项目负责人'
        middleYear              type: 'text', comment: '预期中期'
        knotYear                type: 'text', comment: '预期结项'
        name                    type: 'text', comment: '项目名称'
        content                 type: 'text', comment: '主要内容'
        achievements            type: 'text', comment: '预期成果'
        members                 type: 'text', comment: '参与人'
        other                   type: 'text', comment: '其他'
        mainInfoForm            type: 'text', comment: '申报书'
        department              comment: '审核部门'
        dateCreated             comment: '创建日期'
        dateSubmitted           comment: '提交日期'
        checker                 comment: '部门审核人'
        departmentOpinion       type: 'text', comment: '部门意见'
        dateChecked             comment: '部门审核日期'
        approver                comment: '审批人'
        opinionOfUniversity     type: 'text', comment: '学校意见'
        dateApproved            comment: '审批日期'
        reviewer                comment: '加签人'
        dateReviewed            comment: '加签日期'
        status                  sqlType: 'tm.state', type: StateUserType, comment: '审批进度、状态'
        reason                  type: 'text', comment: '变更原因'
        title                   type: 'text', comment: '职称'
        degree                  type: 'text', comment: '学位'
        email                   type: 'text', comment: '邮箱'
        office                  type: 'text', comment: '行政岗位'
        phone                   type: 'text', comment: '电话'
    }

    static constraints = {
        dateApproved            nullable: true
        opinionOfUniversity     nullable: true
        departmentOpinion       nullable: true
        dateSubmitted           nullable: true
        content                 nullable: true
        other                   nullable: true
        checker                 nullable: true
        dateChecked             nullable: true
        approver                nullable: true
        mainInfoForm            nullable: true
        principal               nullable: true
        middleYear              nullable: true
        knotYear                nullable: true
        name                    nullable: true
        content                 nullable: true
        achievements            nullable: true
        members                 nullable: true
        workflowInstance        nullable: true
        reviewer                nullable: true
        dateReviewed            nullable: true
        reason                  nullable: true
        title                   nullable: true
        degree                  nullable: true
        email                   nullable: true
        office                  nullable: true
        phone                   nullable: true
    }

    String getWorkflowId() {
        WORKFLOW_ID
    }

    static final WORKFLOW_ID = 'hunt.info-change'
}
