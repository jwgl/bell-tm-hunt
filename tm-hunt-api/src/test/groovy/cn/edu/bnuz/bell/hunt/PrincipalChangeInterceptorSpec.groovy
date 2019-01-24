package cn.edu.bnuz.bell.hunt

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class PrincipalChangeInterceptorSpec extends Specification implements InterceptorUnitTest<PrincipalChangeInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test principalChange interceptor matching"() {
        when:"A request matches the interceptor"
        withRequest(controller:"principalChange")

        then:"The interceptor does match"
        interceptor.doesMatch()
    }
}
