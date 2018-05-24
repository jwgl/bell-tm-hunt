package cn.edu.bnuz.bell.hunt

class Subtype {
    String name
    Integer periodOfUniversity
    Integer periodOfCity
    Integer periodOfProvince
    Integer periodOfNation
    Boolean enabled

    static belongsTo = [parent: Type]

    static mapping = {
        comment        '项目类别'
        table          schema: 'tm_hunt'
        id                          generator: 'identity', comment: '类别ID'
        name                        unique: true, length: 20, comment: '类别名称'
        periodOfUniversity          comment: '校级周期'
        periodOfCity                comment: '市级周期'
        periodOfProvince            comment: '省级周期'
        periodOfNation              comment: '国家级周期'
        enabled                     comment: '是否屏蔽'
    }

    static constraints = {
        enabled             nullable: true
        periodOfUniversity  nullable: true
        periodOfCity        nullable: true
        periodOfProvince    nullable: true
        periodOfNation      nullable: true
    }
}
