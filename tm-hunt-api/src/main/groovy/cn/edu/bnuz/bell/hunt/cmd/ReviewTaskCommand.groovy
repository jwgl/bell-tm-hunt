package cn.edu.bnuz.bell.hunt.cmd

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReviewTaskCommand {
    String title
    String content
    String startDate
    String endDate
    String type
    String remind
    String ban
    String attach

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
