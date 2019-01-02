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
     * 变更申请明细
     */
    Teacher principal
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
        middleYear              comment: '预期中期'
        knotYear                comment: '预期结项'
        name                    length: 50, comment: '项目名称'
        content                 length: 2000, comment: '主要内容'
        achievements            length: 2000, comment: '预期成果'
        members                 length: 50, comment: '参与人'
        other                   length: 500, comment: '其他'
        mainInfoForm            length: 50, comment: '申报书'
        department              comment: '审核部门'
        dateCreated             comment: '创建日期'
        dateSubmitted           comment: '提交日期'
        checker                 comment: '部门审核人'
        departmentOpinion       length: 500, comment: '部门意见'
        dateChecked             comment: '部门审核日期'
        approver                comment: '审批人'
        opinionOfUniversity     length: 500, comment: '学校意见'
        dateApproved            comment: '审批日期'
        status                  sqlType: 'tm.state', type: StateUserType, comment: '审批进度、状态'
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
    }

    String getWorkflowId() {
        WORKFLOW_ID
    }

    static final WORKFLOW_ID = 'hunt.info-change'
}
