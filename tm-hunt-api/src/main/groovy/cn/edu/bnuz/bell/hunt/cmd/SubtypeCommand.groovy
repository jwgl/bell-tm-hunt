package cn.edu.bnuz.bell.hunt.cmd

class SubtypeCommand {
    Long parentId
    String parentName
    String name
    String level
    Integer period
    Boolean enabled

    String toString() {
        return "parentId:${parentId}, parentName:${parentName}, name:${name}, level:${level}, period:${period}, enabled:${enabled}"
    }
}
