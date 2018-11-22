package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateObject
import cn.edu.bnuz.bell.workflow.StateUserType
import cn.edu.bnuz.bell.workflow.WorkflowInstance

import java.time.LocalDate

class Review  implements StateObject{

    /**
     * 审核安排
     */
    ReviewTask reviewTask

    /**
     * 项目
     */
    Project project

    /**
     * 审核部门
     */
    Department department

    /**
     * 创建日期
     */
    LocalDate dateCreated

    /**
     * 修改时间
     */
    Date dateModified

    /**
     * 提交时间
     */
    Date dateSubmitted

    /**
     * 部门意见
     */
    String departmentOpinion

    /**
     * 审核人
     */
    Teacher checker

    /**
     * 审核时间
     */
    Date dateChecked

    /**
     * 审批人
     */
    Teacher approver

    /**
     * 学校意见
     */
    String opinionOfUniversity

    /**
     * 审批时间
     */
    Date dateApproved

    /**
     * 状态
     */
    State status

    /**
     * 部门审核结论
     */
    Conclusion departmentConclusion

    /**
     * 学校评审结论
     */
    Conclusion conclusionOfUniversity

    /**
     * 省厅评审结论
     */
    Conclusion conclusionOfProvince

    /**
     * 省厅意见
     */
    String opinionOfProvince

    /**
     * 报告类型，1：立项，2：年度，3：中检，4：结项
     */
    Integer reportType

    /**
     * 主要内容或特色、进展正文等
     */
    String content

    /**
     * 预期成果、未完成原因、主要成果等
     */
    String further

    /**
     * 其他说明、成果应用情况
     */
    String other

    /**
     * 锁定
     */
    Boolean locked

    /**
     * 申报书、中期验收登记表、结项验收登记表
     */
    String mainInfoTable

    /**
     * 主要佐证材料
     */
    String proofFile

    /**
     * 总结报告
     */
    String summaryReport

    /**
     * 工作流实例
     */
    WorkflowInstance workflowInstance

    static hasMany = [expertReview: ExpertReview]

    static belongsTo = [project: Project]

    static mapping = {
        comment                 '项目审核'
        table                   schema: 'tm_hunt'

        id                      generator: 'identity', comment: '审核ID'
        reviewTask              comment: '审核安排'
        project                 comment: '项目'
        department              comment: '审核部门'
        dateCreated             comment: '创建日期'
        dateModified            comment: '修改日期'
        checker                 comment: '部门审核人'
        departmentConclusion    sqlType: 'tm_hunt.conclusion', type:ConclusionUserType, comment: '部门结论'
        departmentOpinion       length: 500, comment: '部门意见'
        dateChecked             comment: '部门审核日期'
        approver                comment: '审批人'
        conclusionOfUniversity  sqlType: 'tm_hunt.conclusion', type:ConclusionUserType, comment: '学校结论'
        opinionOfUniversity     length: 500, comment: '学校意见'
        opinionOfProvince       length: 500, comment: '省厅意见'
        dateApproved            comment: '审批日期'
        status                  sqlType: 'tm.state', type: StateUserType, comment: '审批进度、状态'
        reportType              comment: '报告类型'
        content                 length: 1500, comment: '主要内容或特色、进展正文'
        further                 length: 1500, comment: '预期成果、未完成原因、主要成果等'
        other                   length: 1500, comment: '其他说明、成果应用情况'
        locked                  comment: '锁定'
        conclusionOfProvince    sqlType: 'tm_hunt.conclusion', type:ConclusionUserType, comment: '省厅结论'
        mainInfoTable           length: 50, comment: '申报书、中期验收登记表、结项验收登记表'
        proofFile               length: 50, comment: '主要佐证材料'
        summaryReport           length: 50, comment: '总结报告'
    }

    static constraints = {
        dateApproved            nullable: true
        opinionOfUniversity     nullable: true
        departmentOpinion       nullable: true
        dateSubmitted           nullable: true
        dateModified            nullable: true
        content                 nullable: true
        further                 nullable: true
        other                   nullable: true
        workflowInstance        nullable: true
        checker                 nullable: true
        dateChecked             nullable: true
        approver                nullable: true
        dateApproved            nullable: true
        locked                  nullable: true
        departmentConclusion    nullable: true
        conclusionOfProvince    nullable: true
        conclusionOfUniversity  nullable: true
        opinionOfProvince       nullable: true
        mainInfoTable           nullable: true
        proofFile               nullable: true
        summaryReport           nullable: true
    }

    String getWorkflowId() {
        WORKFLOW_ID
    }

    static final WORKFLOW_ID = 'hunt.review'
}
