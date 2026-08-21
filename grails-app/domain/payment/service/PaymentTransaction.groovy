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

        // Composite index for listing payments by merchant and status, ordered by date
        merchant index: 'idx_payment_merchant_status_created'
        status index: 'idx_payment_merchant_status_created'
        dateCreated index: 'idx_payment_merchant_status_created'
    }

    static constraints = {
        // A reference is only unique for its owning merchant.
        reference blank: false, unique: ['merchant']
        amount min: 0.01G
        currency blank: false
        description nullable: true
        merchant nullable: false
        status nullable: false
    }
}
