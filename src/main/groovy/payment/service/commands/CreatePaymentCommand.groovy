package payment.service.commands

import java.math.BigDecimal
import grails.validation.Validateable

/**
 * Validates the request payload before it reaches PaymentService.
 */
class CreatePaymentCommand implements Validateable {
    String reference
    BigDecimal amount
    String currency
    String description

    static constraints = {
        reference blank: false, nullable: false
        amount nullable: false, min: new BigDecimal('0.01')
        currency blank: false, nullable: false
        description nullable: true
    }

}
