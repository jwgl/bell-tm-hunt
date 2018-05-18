package cn.edu.bnuz.bell.hunt

class Type {
    String      name

    static hasMany = [subtype: Subtype]
    static mapping = {
        comment                 '主类'
        table                   schema: 'tm_hunt'
        id                      generator: 'identity', comment: '主类ID'
        name                    unique: true, length: 20, comment: '主类名称'
    }
}
