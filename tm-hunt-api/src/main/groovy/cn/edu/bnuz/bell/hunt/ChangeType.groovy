package cn.edu.bnuz.bell.hunt

import groovy.transform.CompileStatic

/**
 * 变更内容
 */
@CompileStatic
enum ChangeType {
    PRINCIPAL, // 项目负责人
    DELAY, // 延期
    NAME, // 项目名称
    CONTENT, // 研究内容重大调整
    TERMINATION, // 自行中止项目
    ACHIEVEMENTS, // 改变成果形式
    MEMBERS, // 变更参与人
    OTHER, // 其他
}
