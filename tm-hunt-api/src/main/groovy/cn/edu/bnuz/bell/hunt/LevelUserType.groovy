package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.orm.PostgreSQLEnumUserType
import groovy.transform.CompileStatic

@CompileStatic
class LevelUserType extends PostgreSQLEnumUserType{
    @Override
    Class<? extends Enum> getEnumClass() {
        Level
    }
}
