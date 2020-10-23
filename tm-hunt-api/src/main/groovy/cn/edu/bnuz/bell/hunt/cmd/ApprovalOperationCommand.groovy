package cn.edu.bnuz.bell.hunt.cmd

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ApprovalOperationCommand {
    String conclusionOfUniversity
    String opinionOfUniversity
    String conclusionOfProvince
    String opinionOfProvince
    Boolean removeExperts
    String code
    String dateStarted
    Integer middleYear
    Integer knotYear
    String dateFinished

    static LocalDate toDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        if(dateStr!=null){
            LocalDate date=LocalDate.parse(dateStr, formatter)
            return date
        }else{
            return null
        }
    }
}