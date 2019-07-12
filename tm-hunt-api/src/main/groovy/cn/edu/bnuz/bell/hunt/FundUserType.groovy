package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.orm.PostgreSQLEnumUserType

class FundUserType extends PostgreSQLEnumUserType{
    @Override
    Class<? extends Enum> getEnumClass() {
        FundType
    }
}
