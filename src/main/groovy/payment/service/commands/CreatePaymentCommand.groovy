package payment.service.commands

import java.math.BigDecimal

/**
 * Command object for creating a payment
 */
class CreatePaymentCommand {
    String reference
    BigDecimal amount
    String currency
    String description

    static constraints = {
        reference blank: false, nullable: false, unique: true
        amount nullable: false, min: new BigDecimal('0.01')
        currency blank: false, nullable: false
        description nullable: true
    }

    CreatePaymentCommand() {}

    CreatePaymentCommand(Map data) {
        this.reference = data?.reference
        this.amount = data?.amount as BigDecimal
        this.currency = data?.currency
        this.description = data?.description
    }
}
