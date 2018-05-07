package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.orm.PostgreSQLEnumUserType

class ReviewTypeUserType extends PostgreSQLEnumUserType{
    @Override
    Class<? extends Enum> getEnumClass() {
        ReviewType
    }
}
