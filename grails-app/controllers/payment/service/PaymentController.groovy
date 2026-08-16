package payment.service

import payment.service.api.PaymentResponse
import payment.service.commands.CreatePaymentCommand

class PaymentController extends BaseApiController {

    PaymentService paymentService

    def save() {
        handleRequest {
            def cmd = new CreatePaymentCommand(request.JSON ?: [:])
            def payment = paymentService.processPayment(request.getHeader('X-API-KEY'), cmd
            )
            renderJson(201, PaymentResponse.toSummary(payment))
        }
    }

    def capture() {
        handleRequest {
            def payment = paymentService.capturePayment(
                request.getHeader('X-API-KEY'),
                params.reference
            )
            renderJson(200, PaymentResponse.toSummary(payment))
        }
    }

    def refund() {
        handleRequest {
            def payment = paymentService.refundPayment(
                request.getHeader('X-API-KEY'),
                params.reference
            )
            renderJson(200, PaymentResponse.toSummary(payment))
        }
    }

    def show() {
        handleRequest {
            def payment = paymentService.getPayment(request.getHeader('X-API-KEY'), params.reference)
            renderJson(200, PaymentResponse.toDetail(payment))
        }
    }

    def index() {
        handleRequest {
            def response = paymentService.listPaymentsPaginated(
                request.getHeader('X-API-KEY'),
                params
            )
            renderJson(200, response)
        }
    }
}
