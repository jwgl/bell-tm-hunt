package tm.hunt.api

import cn.edu.bnuz.bell.hunt.Conclusion
import cn.edu.bnuz.bell.hunt.FundType
import cn.edu.bnuz.bell.hunt.Level
import cn.edu.bnuz.bell.hunt.ReviewType
import cn.edu.bnuz.bell.hunt.Status
import grails.converters.JSON

class BootStrap {

    def init = { servletContext ->
        JSON.registerObjectMarshaller(Level) {
            it.name()
        }
        JSON.registerObjectMarshaller(Conclusion) {
            it.name()
        }
        JSON.registerObjectMarshaller(ReviewType) {
            it.name()
        }
        JSON.registerObjectMarshaller(Status) {
            it.name()
        }
        JSON.registerObjectMarshaller(FundType) {
            it.name()
        }
    }
    def destroy = {
    }
}
