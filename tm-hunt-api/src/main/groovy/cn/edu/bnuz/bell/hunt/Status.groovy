package cn.edu.bnuz.bell.hunt

import groovy.transform.CompileStatic

/**
 * 建设情况
 */
@CompileStatic
enum Status {
    CREATED, // 新建
    INHAND, // 在研
    FINISHED, // 结题
    CUTOUT // 终止
}