package cn.edu.bnuz.bell.hunt

import cn.edu.bnuz.bell.http.ForbiddenException
import cn.edu.bnuz.bell.hunt.cmd.SubtypeCommand
import grails.gorm.transactions.Transactional

@Transactional
class TypeService {

    def typeList() {
        Type.executeQuery('select new map(id as id, name as name) from Type')
    }

    def subtypeList() {
        Subtype.executeQuery'''
select new map(
    st.id as id,
    st.name as name,
    st.enabled as enabled,
    st.level as level,
    st.period as period,
    p.name as parentName
) from Subtype st join st.parent p
'''
    }

    def createType(String name) {
        if (!Type.findByName(name)) {
            def type = new Type(name: name)
            type.save()
        }
    }

    def createSubtype(SubtypeCommand cmd) {
        def type = Type.get(cmd.parentId)
        if (!type) {
            if (cmd.parentName) {
                type = createType(cmd.parentName)
            } else {
                throw new ForbiddenException()
            }
        }
        def subtype = new Subtype(
                parent: type,
                level: cmd.level as Level,
                enabled: cmd.enabled,
                name: cmd.name,
                period: cmd.period
        )
        if (!subtype.save()) {
            subtype.errors.each{
                println it
            }
        }
        return subtype
    }
}
