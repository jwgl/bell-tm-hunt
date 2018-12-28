package cn.edu.bnuz.bell.hunt

class ChangeItem {
    /**
     * 变更的属性
     */
    String key

    /**
     * 变更的内容
     */
    String content

    static belongsTo = [infoChane: InfoChange]

    static mapping = {
        comment                 '变更明细'
        table                   schema: 'tm_hunt'
        id                      generator: 'identity', comment: '明细ID'
        key                     length: 50, comment: '变更属性'
        content                 length: 2000, comment: '变更内容'
    }
}
