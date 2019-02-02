package cn.edu.bnuz.bell.hunt.cmd

class SubtypeCommand {
    Long parentId
    String parentName
    String name
    Integer periodOfUniversity
    Integer periodOfCity
    Integer periodOfProvince
    Integer periodOfNation
    Boolean enabled

    String toString() {
        return "parentId:${parentId}, parentName:${parentName}, name:${name}, periodOfCity:${periodOfCity}, periodOfUniversity:${periodOfUniversity}, periodOfProvince:${periodOfProvince}, enabled:${enabled}"
    }
}
