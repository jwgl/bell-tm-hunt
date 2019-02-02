package cn.edu.bnuz.bell.hunt

import groovy.transform.CompileStatic

/**
 * 审核结论
 */
@CompileStatic
enum Conclusion {
    OK, // 通过
    VETO, // 不通过
    DELAY // 暂缓通过
}