package cn.edu.bnuz.bell.hunt

class Origin {
    String name

    static mapping = {
        comment '项目来源'
        table schema: 'tm_hunt'
        name length: 50, comment: '来源名称'
    }
}
