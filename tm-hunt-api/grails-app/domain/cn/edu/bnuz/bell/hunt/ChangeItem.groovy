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
        key                     comment: '变更属性'
        content                 type: 'text', comment: '变更内容'
    }

    static constraints = {
        content     nullable: true
    }
}
