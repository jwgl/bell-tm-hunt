package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.orm.PostgreSQLEnumUserType
import groovy.transform.CompileStatic

@CompileStatic
class ConclusionUserType extends PostgreSQLEnumUserType{
    @Override
    Class<? extends Enum> getEnumClass() {
        Conclusion
    }
}
