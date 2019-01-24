package cn.edu.bnuz.bell.hunt

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ApplicationCheckInterceptorSpec extends Specification implements InterceptorUnitTest<ApplicationCheckInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test applicationCheck interceptor matching"() {
        when:"A request matches the interceptor"
        withRequest(controller:"applicationCheck")

        then:"The interceptor does match"
        interceptor.doesMatch()
    }
}
