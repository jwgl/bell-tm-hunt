package cn.edu.bnuz.bell.hunt.cmd

class ExpertReviewCommand {
    String conclusion
    String opinion
    private Integer value

    void setValue(Integer value) {
        this.value = value
    }

    Integer getValue() {
        return this.value ? this.value : 0
    }
}
