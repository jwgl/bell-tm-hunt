package cn.edu.bnuz.bell.hunt

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ApplicationInterceptorSpec extends Specification implements InterceptorUnitTest<ApplicationInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test application interceptor matching"() {
        when:"A request matches the interceptor"
        withRequest(controller:"application")

        then:"The interceptor does match"
        interceptor.doesMatch()
    }
}
