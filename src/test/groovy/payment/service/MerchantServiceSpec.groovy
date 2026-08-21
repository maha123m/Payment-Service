package payment.service

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import payment.service.commands.CreateMerchantCommand
import payment.service.exception.BusinessException
import payment.service.exception.ErrorCode
import spock.lang.Specification

class MerchantServiceSpec extends Specification implements ServiceUnitTest<MerchantService>, DataTest {

    Class<?>[] getDomainClassesToMock() {
        [Merchant]
    }

    void 'createMerchant generates api key and secret key and persists merchant'() {
        when:
        def cmd = new CreateMerchantCommand(name: 'Test Store', email: 'store@test.com')
        def merchant = service.createMerchant(cmd)

        then:
        merchant.id
        merchant.name == 'Test Store'
        merchant.email == 'store@test.com'
        merchant.apiKey
        merchant.secretKey
        merchant.secretKey.length() == 64 // 256 bits = 64 hex characters
        merchant.active
    }

    void 'createMerchant rejects duplicate email'() {
        given:
        def cmd1 = new CreateMerchantCommand(name: 'Store A', email: 'store@test.com')
        service.createMerchant(cmd1)

        when:
        def cmd2 = new CreateMerchantCommand(name: 'Store B', email: 'store@test.com')
        service.createMerchant(cmd2)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.MERCHANT_VALIDATION_FAILED.code
    }

    void 'getMerchantByApiKey rejects missing key'() {
        when:
        service.getMerchantByApiKey(null)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.API_KEY_MISSING.code
        ex.httpStatus == 401
    }

    void 'getMerchantByApiKey rejects inactive merchant'() {
        given:
        def cmd = new CreateMerchantCommand(name: 'Inactive Store', email: 'inactive@test.com')
        def merchant = service.createMerchant(cmd)
        merchant.active = false
        merchant.save(flush: true)

        when:
        service.getMerchantByApiKey(merchant.apiKey)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.MERCHANT_INACTIVE.code
        ex.httpStatus == 403
    }
}
