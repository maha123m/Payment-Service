package payment.service

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import payment.service.commands.CreatePaymentCommand
import payment.service.exception.BusinessException
import payment.service.exception.ErrorCode
import spock.lang.Specification

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class PaymentRequestSignatureServiceSpec extends Specification implements ServiceUnitTest<PaymentRequestSignatureService>, DataTest {

    MerchantService merchantService

    Class<?>[] getDomainClassesToMock() {
        [Merchant, PaymentTransaction]
    }

    def setup() {
        merchantService = Mock(MerchantService)
        service.merchantService = merchantService
    }

    private String calculateHmac(String secretKey, String data) {
        Mac mac = Mac.getInstance('HmacSHA256')
        mac.init(new SecretKeySpec(secretKey.getBytes('UTF-8'), 'HmacSHA256'))
        mac.doFinal(data.getBytes('UTF-8')).encodeHex().toString()
    }

    void 'valid signature accepts request'() {
        given:
        def merchant = new Merchant(
            id: 1L,
            name: 'Test Store',
            email: 'store@test.com',
            apiKey: 'merchant_abc123',
            secretKey: 'test_secret_key_123456789012345678901234',
            active: true
        ).save(flush: true)

        merchantService.getMerchantByApiKey('merchant_abc123') >> merchant

        String requestBody = '{"amount":100,"currency":"USD","reference":"PAY-123"}'
        String signature = calculateHmac(merchant.secretKey, requestBody)

        when:
        service.verifyCreatePayment('merchant_abc123', signature, requestBody)

        then:
        noExceptionThrown()
    }

    void 'missing X-API-KEY header is rejected'() {
        given:
        String requestBody = '{"amount":100,"currency":"USD"}'

        when:
        service.verifyCreatePayment(null, 'signature', requestBody)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.API_KEY_MISSING.code
        ex.httpStatus == 401
    }

    void 'missing X-SIGNATURE header is rejected'() {
        given:
        String requestBody = '{"amount":100,"currency":"USD"}'

        when:
        service.verifyCreatePayment('merchant_abc123', null, requestBody)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.SIGNATURE_MISSING.code
        ex.httpStatus == 401
    }

    void 'invalid API key is rejected'() {
        given:
        merchantService.getMerchantByApiKey('invalid_key') >> { throw new BusinessException(ErrorCode.API_KEY_INVALID, 'Invalid API key') }
        String requestBody = '{"amount":100,"currency":"USD"}'

        when:
        service.verifyCreatePayment('invalid_key', 'signature', requestBody)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.API_KEY_INVALID.code
    }

    void 'invalid signature is rejected'() {
        given:
        def merchant = new Merchant(
            id: 1L,
            name: 'Test Store',
            email: 'store@test.com',
            apiKey: 'merchant_abc123',
            secretKey: 'test_secret_key_123456789012345678901234',
            active: true
        ).save(flush: true)

        merchantService.getMerchantByApiKey('merchant_abc123') >> merchant

        String requestBody = '{"amount":100,"currency":"USD"}'

        when:
        service.verifyCreatePayment('merchant_abc123', 'invalid_signature', requestBody)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.SIGNATURE_INVALID.code
        ex.httpStatus == 401
    }

    void 'signature computed with wrong secret key fails'() {
        given:
        def merchant = new Merchant(
            id: 1L,
            name: 'Test Store',
            email: 'store@test.com',
            apiKey: 'merchant_abc123',
            secretKey: 'correct_secret_key_1234567890123456789012',
            active: true
        ).save(flush: true)

        merchantService.getMerchantByApiKey('merchant_abc123') >> merchant

        String requestBody = '{"amount":100,"currency":"USD"}'
        // Signature computed with wrong secret key
        String wrongSignature = calculateHmac('wrong_secret_key_1234567890123456789012', requestBody)

        when:
        service.verifyCreatePayment('merchant_abc123', wrongSignature, requestBody)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.SIGNATURE_INVALID.code
        ex.httpStatus == 401
    }

    void 'tampered request body invalidates signature'() {
        given:
        def merchant = new Merchant(
            id: 1L,
            name: 'Test Store',
            email: 'store@test.com',
            apiKey: 'merchant_abc123',
            secretKey: 'test_secret_key_123456789012345678901234',
            active: true
        ).save(flush: true)

        merchantService.getMerchantByApiKey('merchant_abc123') >> merchant

        String originalBody = '{"amount":100,"currency":"USD"}'
        String signature = calculateHmac(merchant.secretKey, originalBody)

        String tamperedBody = '{"amount":10000,"currency":"USD"}'

        when:
        service.verifyCreatePayment('merchant_abc123', signature, tamperedBody)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.SIGNATURE_INVALID.code
        ex.httpStatus == 401
    }

    void 'merchant without secret key is rejected'() {
        given:
        def merchant = new Merchant(
            id: 1L,
            name: 'Test Store',
            email: 'store@test.com',
            apiKey: 'merchant_abc123',
            secretKey: null,
            active: true
        ).save(flush: true)

        merchantService.getMerchantByApiKey('merchant_abc123') >> merchant

        String requestBody = '{"amount":100,"currency":"USD"}'

        when:
        service.verifyCreatePayment('merchant_abc123', 'signature', requestBody)

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.SIGNATURE_INVALID.code
    }

    void 'verifyReferenceAction with valid signature'() {
        given:
        def merchant = new Merchant(
            id: 1L,
            name: 'Test Store',
            email: 'store@test.com',
            apiKey: 'merchant_abc123',
            secretKey: 'test_secret_key_123456789012345678901234',
            active: true
        ).save(flush: true)

        merchantService.getMerchantByApiKey('merchant_abc123') >> merchant

        when:
        service.verifyReferenceAction('merchant_abc123', 'signature')

        then:
        noExceptionThrown()
    }
}
