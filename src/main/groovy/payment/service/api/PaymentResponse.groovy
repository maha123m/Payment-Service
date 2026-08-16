package payment.service.api

import payment.service.PaymentTransaction

class PaymentResponse {

    static Map toSummary(PaymentTransaction payment) {
        [
                reference  : payment.reference,
                amount     : payment.amount,
                currency   : payment.currency,
                description: payment.description,
                status     : payment.status.name(),
                dateCreated: payment.dateCreated
        ]
    }

    static Map toDetail(PaymentTransaction payment) {
        toSummary(payment) + [
                merchant   : payment.merchant.name,
                lastUpdated: payment.lastUpdated
        ]
    }

    static Map toListResponse(List<PaymentTransaction> payments) {
        [
                count   : payments.size(),
                payments: payments.collect { toSummary(it) }
        ]
    }
}
