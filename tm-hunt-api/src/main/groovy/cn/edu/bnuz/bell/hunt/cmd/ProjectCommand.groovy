package cn.edu.bnuz.bell.hunt.cmd

class ProjectCommand {
    Long reviewTaskId
    String principalId
    String title
    String degree
    String email
    String discipline
    String major
    String direction

    String departmentId
    String office
    String phone

    String name
    String urls
    Long subtypeId
    Long originId
    String members
    String content
    String achievements
    String other
    String level
    String mainInfoForm
    List<String> proofFile
    String summaryReport

    /**
     * 立项信息
     */
    String code
    String dateStarted
    Integer middleYear
    Integer knotYear

    String toString() {
        return  "reviewTaskId: ${reviewTaskId}; " +
                "principalId: ${principalId}; " +
                "title: ${title}; " +
                "degree: ${degree}; " +
                "email: ${email}" +
                "discipline: ${discipline}; " +
                "major: ${major}; " +
                "direction: ${direction}; " +
                "departmentId: ${departmentId}; " +
                "office: ${office}; " +
                "phone: ${phone}; " +
                "name: ${name}; " +
                "urls: ${urls}; " +
                "subtypeId: ${subtypeId}; " +
                "originId: ${originId}; " +
                "members: ${members}; " +
                "content: ${content}; " +
                "achievements: ${achievements}; " +
                "level: ${level}" +
                "mainInfoForm: ${mainInfoForm}" +
                "summaryReport: ${summaryReport}"
    }
}
