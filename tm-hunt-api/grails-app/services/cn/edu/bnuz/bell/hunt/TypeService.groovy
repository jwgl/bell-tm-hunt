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
    st.periodOfUniversity as periodOfUniversity,
    st.periodOfCity as periodOfCity,
    st.periodOfProvince as periodOfProvince,
    st.periodOfNation as periodOfNation,
    p.name as parentName,
    p.id as parentId
) from Subtype st join st.parent p
'''
    }

    def getAllSubtypes() {
        Subtype.executeQuery'''
select new map(id as id, name as name) from Subtype where enabled = true
'''
    }

    def getAllSubtypesWithPeriod() {
        Subtype.executeQuery'''
select new map(
id as id,
name as name,
periodOfUniversity as periodOfUniversity,
periodOfCity as periodOfCity,     
periodOfProvince as periodOfProvince,
periodOfNation as periodOfNation
) from Subtype where enabled = true
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
                periodOfUniversity: cmd.periodOfUniversity,
                periodOfCity: cmd.periodOfCity,
                periodOfProvince: cmd.periodOfProvince,
                periodOfNation: cmd.periodOfNation,
                enabled: cmd.enabled,
                name: cmd.name
        )
        if (!subtype.save()) {
            subtype.errors.each{
                println it
            }
        }
        return subtype
    }

    def update(Long id, SubtypeCommand cmd) {
        Subtype form = Subtype.load(id)
        if (form) {
            form.parent = Type.load(cmd.parentId)
            form.periodOfUniversity = cmd.periodOfUniversity
            form.periodOfCity = cmd.periodOfCity
            form.periodOfProvince = cmd.periodOfProvince
            form.periodOfNation = cmd.periodOfNation
            form.enabled = cmd.enabled
            form.name = cmd.name
        }
        form .save()
    }
}
