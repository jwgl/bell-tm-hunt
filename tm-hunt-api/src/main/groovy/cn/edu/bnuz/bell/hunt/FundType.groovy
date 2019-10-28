package cn.edu.bnuz.bell.hunt

import groovy.transform.CompileStatic

/**
 * 经费类型：枚举型，为了转换成JSON时能正确显示，还须在init/BootStrap下注册
 */
@CompileStatic
enum FundType {
    TRANSFER, // 划拨经费
    ARRANGEMENT //计划安排
}
