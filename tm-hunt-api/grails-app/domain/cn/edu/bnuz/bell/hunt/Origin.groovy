package cn.edu.bnuz.bell.hunt

class Origin {
    String name

    static mapping = {
        comment '项目来源'
        table schema: 'tm_hunt'
        id   generator: 'identity', comment: '来源ID'
        name type: 'text', comment: '来源名称'
    }
}
