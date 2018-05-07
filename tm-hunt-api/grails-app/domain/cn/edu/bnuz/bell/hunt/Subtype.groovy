package cn.edu.bnuz.bell.hunt

class Subtype {
    String name
    Level level
    Integer period

    static belongsTo = [parent: Type]

    static mapping = {
        comment        '项目类别'
        table          schema: 'tm_hunt'
        id             generator: 'identity', comment: '类别ID'
        name           length: 20, comment: '类别名称'
        level          sqlType: 'tm_hunt.level', type: LevelUserType, comment: '项目等级'
        period         comment: '建设周期'
    }
}
