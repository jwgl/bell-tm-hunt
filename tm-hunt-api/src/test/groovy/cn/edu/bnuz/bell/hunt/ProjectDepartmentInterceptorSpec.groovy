package cn.edu.bnuz.bell.hunt

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ProjectDepartmentInterceptorSpec extends Specification implements InterceptorUnitTest<ProjectDepartmentInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test projectDepartment interceptor matching"() {
        when:"A request matches the interceptor"
        withRequest(controller:"projectDepartment")

        then:"The interceptor does match"
        interceptor.doesMatch()
    }
}
