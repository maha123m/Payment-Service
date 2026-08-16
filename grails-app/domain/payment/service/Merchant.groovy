package payment.service

class Merchant {

    String name
    String email
    String apiKey
    Boolean active = true
    Date dateCreated
    Date lastUpdated

    static hasMany = [payments: PaymentTransaction]

    static constraints = {
        name blank: false
        email blank: false, unique: true, email: true
        apiKey nullable: true, unique: true
    }

    def beforeInsert() {
        if (!apiKey) {
            apiKey = "merchant_" + UUID.randomUUID().toString().replace('-', '')
        }
    }
}
