package cn.edu.bnuz.bell.hunt.cmd

class InfoChangeCommand {
    Long projectId
    Integer[] type
    String principalId
    Integer middleYear
    Integer knotYear
    String name
    String content
    String achievements
    String members
    String other
    String mainInfoForm

    String toString() {
        return  "principalId: ${projectId}; " +
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
