package payment.service

import payment.service.api.PaymentResponse
import payment.service.commands.CreatePaymentCommand

class PaymentController extends BaseApiController {

    PaymentService paymentService
    PaymentRequestSignatureService paymentRequestSignatureService

    def save() {
        handleRequest {
            String apiKey = request.getHeader('X-API-KEY')
            String rawBody = request.inputStream.getText('UTF-8')

            paymentRequestSignatureService.verifyCreatePayment(
                    apiKey,
                    request.getHeader('X-SIGNATURE'),
                    rawBody
            )

            def cmd = paymentService.parsePaymentCommand(rawBody)

            if (!validateCommand(cmd)) {
                return
            }

            def payment = paymentService.processPayment(apiKey, cmd)
            renderJson(201, PaymentResponse.toSummary(payment))
        }
    }

    def capture() {
        handleRequest {
            String apiKey = request.getHeader('X-API-KEY')
            paymentRequestSignatureService.verifyReferenceAction(
                    apiKey,
                    request.getHeader('X-SIGNATURE')
            )
            def payment = paymentService.capturePayment(
                apiKey,
                params.reference
            )
            renderJson(200, PaymentResponse.toSummary(payment))
        }
    }

    def refund() {
        handleRequest {
            String apiKey = request.getHeader('X-API-KEY')
            paymentRequestSignatureService.verifyReferenceAction(
                    apiKey,
                    request.getHeader('X-SIGNATURE')
            )
            def payment = paymentService.refundPayment(
                apiKey,
                params.reference
            )
            renderJson(200, PaymentResponse.toSummary(payment))
        }
    }

    def show() {
        handleRequest {
            String apiKey = request.getHeader('X-API-KEY')
            paymentRequestSignatureService.verifyReferenceAction(
                    apiKey,
                    request.getHeader('X-SIGNATURE')
            )
            def payment = paymentService.getPayment(apiKey, params.reference)
            renderJson(200, PaymentResponse.toDetail(payment))
        }
    }

    def index() {
        handleRequest {
            String apiKey = request.getHeader('X-API-KEY')
            paymentRequestSignatureService.verifyReferenceAction(
                    apiKey,
                    request.getHeader('X-SIGNATURE')
            )
            def response = paymentService.listPaymentsPaginated(
                apiKey,
                params
            )
            renderJson(200, response)
        }
    }
}
