package payment.service

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import payment.service.commands.CreateMerchantCommand
import payment.service.commands.CreatePaymentCommand
import payment.service.enums.PaymentStatus
import payment.service.exception.BusinessException
import payment.service.exception.ErrorCode
import spock.lang.Specification

class PaymentServiceSpec extends Specification implements ServiceUnitTest<PaymentService>, DataTest {

    MerchantService merchantService

    Class<?>[] getDomainClassesToMock() {
        [Merchant, PaymentTransaction]
    }

    void setup() {
        merchantService = Mock(MerchantService)
        service.merchantService = merchantService
    }

    void 'processPayment creates pending payment'() {
        given:
        def merchant = new Merchant(id: 1L, name: 'Store', email: 'store@test.com', apiKey: 'key-1', active: true)
        merchantService.getMerchantByApiKey('key-1') >> merchant

        when:
        def cmd = new CreatePaymentCommand(
            reference: 'INV-10001',
            amount: 120.50,
            currency: 'USD',
            description: 'Order payment'
        )
        def payment = service.processPayment('key-1', cmd)

        then:
        payment.reference == 'INV-10001'
        payment.amount == 120.50
        payment.currency == 'USD'
        payment.status == PaymentStatus.PENDING
        payment.merchant == merchant
    }

    void 'processPayment allows the same reference for different merchants'() {
        given:
        def firstMerchant = new Merchant(id: 1L, name: 'Store One', email: 'one@test.com', apiKey: 'key-1', active: true)
        def secondMerchant = new Merchant(id: 2L, name: 'Store Two', email: 'two@test.com', apiKey: 'key-2', active: true)
        merchantService.getMerchantByApiKey('key-1') >> firstMerchant
        merchantService.getMerchantByApiKey('key-2') >> secondMerchant

        def command = new CreatePaymentCommand(reference: 'INV-10001', amount: 120.50, currency: 'USD')

        when:
        def firstPayment = service.processPayment('key-1', command)
        def secondPayment = service.processPayment('key-2', command)

        then:
        firstPayment.reference == secondPayment.reference
        firstPayment.merchant.id != secondPayment.merchant.id
    }

    void 'processPayment rejects a duplicate reference for the same merchant'() {
        given:
        def merchant = new Merchant(id: 1L, name: 'Store', email: 'store@test.com', apiKey: 'key-1', active: true)
        merchantService.getMerchantByApiKey('key-1') >> merchant
        def command = new CreatePaymentCommand(reference: 'INV-10001', amount: 120.50, currency: 'USD')
        service.processPayment('key-1', command)

        when:
        service.processPayment('key-1', command)

        then:
        def exception = thrown(BusinessException)
        exception.errorCode == ErrorCode.PAYMENT_DUPLICATE_REFERENCE.code
    }

    void 'capturePayment transitions pending payment to success'() {
        given:
        def merchant = new Merchant(id: 1L, name: 'Store', email: 'store@test.com', apiKey: 'key-1', active: true)
        merchantService.getMerchantByApiKey('key-1') >> merchant

        def cmd = new CreatePaymentCommand(
            reference: 'INV-10002',
            amount: 50.00,
            currency: 'USD'
        )
        service.processPayment('key-1', cmd)

        when:
        def payment = service.capturePayment('key-1', 'INV-10002')

        then:
        payment.status == PaymentStatus.SUCCESS
    }

    void 'capturePayment rejects already captured payment'() {
        given:
        def merchant = new Merchant(id: 1L, name: 'Store', email: 'store@test.com', apiKey: 'key-1', active: true)
        merchantService.getMerchantByApiKey('key-1') >> merchant

        def cmd = new CreatePaymentCommand(
            reference: 'INV-10003',
            amount: 50.00,
            currency: 'USD'
        )
        service.processPayment('key-1', cmd)
        service.capturePayment('key-1', 'INV-10003')

        when:
        service.capturePayment('key-1', 'INV-10003')

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.CAPTURE_INVALID_STATUS.code
    }

    void 'refundPayment transitions success payment to refunded'() {
        given:
        def merchant = new Merchant(id: 1L, name: 'Store', email: 'store@test.com', apiKey: 'key-1', active: true)
        merchantService.getMerchantByApiKey('key-1') >> merchant

        def cmd = new CreatePaymentCommand(
            reference: 'INV-10004',
            amount: 75.00,
            currency: 'USD'
        )
        service.processPayment('key-1', cmd)
        service.capturePayment('key-1', 'INV-10004')

        when:
        def payment = service.refundPayment('key-1', 'INV-10004')

        then:
        payment.status == PaymentStatus.REFUNDED
    }

    void 'refundPayment rejects pending payment'() {
        given:
        def merchant = new Merchant(id: 1L, name: 'Store', email: 'store@test.com', apiKey: 'key-1', active: true)
        merchantService.getMerchantByApiKey('key-1') >> merchant

        def cmd = new CreatePaymentCommand(
            reference: 'INV-10005',
            amount: 75.00,
            currency: 'USD'
        )
        service.processPayment('key-1', cmd)

        when:
        service.refundPayment('key-1', 'INV-10005')

        then:
        def ex = thrown(BusinessException)
        ex.errorCode == ErrorCode.REFUND_INVALID_STATUS.code
    }
}
