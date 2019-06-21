package cn.edu.bnuz.bell.hunt.cmd

class ExpertReviewCommand {
    String conclusion
    String opinion
    Boolean isCommit
    private Integer score

    void setScore(Integer value) {
        this.score = value
    }

    Integer getScore() {
        return this.score ? this.score : 0
    }
}
