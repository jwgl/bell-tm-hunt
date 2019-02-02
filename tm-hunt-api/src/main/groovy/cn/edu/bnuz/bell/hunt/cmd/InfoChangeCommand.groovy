package cn.edu.bnuz.bell.hunt.cmd

class InfoChangeCommand {
    Long projectId
    Integer[] type
    String reason
    Integer middleYear
    Integer knotYear
    String name
    String content
    String achievements
    String members
    String other
    String mainInfoForm
    /**
     * 负责人变更
     */
    String principalId
    String title
    String degree
    String email
    String office
    String phone

    String toString() {
        return  "projectId: ${projectId}; " +
                "principalId: ${principalId}; " +
                "reason: ${reason}" +
                "degree: ${degree}" +
                "name: ${name}; " +
                "middleYear: ${middleYear}; " +
                "knotYear: ${knotYear}; " +
                "members: ${members}; " +
                "content: ${content}; " +
                "achievements: ${achievements}; " +
                "mainInfoForm: ${mainInfoForm}" +
                "other: ${other}" +
                "type: ${type.join(';')}"
    }
}
