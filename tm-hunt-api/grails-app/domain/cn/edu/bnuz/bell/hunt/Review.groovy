package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.organization.Department
import cn.edu.bnuz.bell.organization.Teacher
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.StateUserType
import cn.edu.bnuz.bell.workflow.WorkflowInstance

import java.time.LocalDate

class Review {

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
    String finalOpinion

    /**
     * 审批时间
     */
    Date dateApproved

    /**
     * 状态
     */
    State status

    /**
     * 专家意见
     */
    String expertOpinion

    /**
     * 评审结果
     */
    Conclusion conclusion

    /**
     * 检查类型
     */
    ReviewType reviewType

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
     * 工作流实例
     */
    WorkflowInstance workflowInstance

    static hasMany = [expertReview: ExpertReview]

    static belongsTo = [project: Project]

    static mapping = {
        comment                 '项目审核'
        table                   schema: 'tm_hunt'

        reviewTask              comment: '审核安排'
        project                 comment: '项目'
        department              comment: '审核部门'
        dateCreated             comment: '创建日期'
        dateModified            comment: '修改日期'
        checker                 comment: '部门审核人'
        departmentOpinion       length: 500, comment: '部门意见'
        dateChecked             comment: '部门审核日期'
        approver                comment: '审批人'
        finalOpinion            length: 500, comment: '学校意见'
        dateApproved            comment: '审批日期'
        status                  sqlType: 'tm.state', type: StateUserType, comment: '审批进度、状态'
        conclusion              sqlType: 'tm_hunt.conclusion', type:ConclusionUserType, comment: '结论'
        expertOpinion           length: 1500, comment: '专家意见'
        reviewType              sqlType: 'tm_hunt.review_type', type: ReviewTypeUserType, comment: '检查类型'
        content                 length: 1500, comment: '主要内容或特色、进展正文'
        further                 length: 1500, comment: '预期成果、未完成原因、主要成果等'
        other                   length: 1500, comment: '其他说明、成果应用情况'
    }

    static constraints = {
        conclusion          nullable: true
        expertOpinion       nullable: true
        dateApproved        nullable: true
        finalOpinion        nullable: true
        departmentOpinion   nullable: true
        dateSubmitted       nullable: true
        dateModified        nullable: true
        further             nullable: true
        other               nullable: true
        conclusion          nullable: true
        workflowInstance    nullable: true
    }

    String getWorkflowId() {
        WORKFLOW_ID
    }

    static final WORKFLOW_ID = 'hunt.review'
}
