package payment.service

import payment.service.enums.PaymentStatus

class PaymentTransaction {

    String reference
    BigDecimal amount
    String currency
    String description
    PaymentStatus status = PaymentStatus.PENDING
    Merchant merchant
    Date dateCreated
    Date lastUpdated

    static belongsTo = [merchant: Merchant]

    static mapping = {
        version true
    }

    static constraints = {
        reference blank: false, unique: true
        amount min: 0.01G
        currency blank: false
        description nullable: true
        merchant nullable: false
        status nullable: false
    }
}
