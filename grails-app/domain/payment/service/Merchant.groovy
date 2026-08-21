package payment.service

import java.security.SecureRandom

class Merchant {

    String name
    String email
    String apiKey
    String secretKey
    Boolean active = true
    Date dateCreated
    Date lastUpdated

    static hasMany = [payments: PaymentTransaction]

    static constraints = {
        name blank: false
        email blank: false, unique: true, email: true
        apiKey nullable: true, unique: true
        secretKey nullable: true, unique: true
    }


    def beforeInsert() {
        if (!apiKey) {
            apiKey = "merchant_" + UUID.randomUUID().toString().replace('-', '')
        }
        if (!secretKey) {
            secretKey = generateSecureSecret()
        }
    }

    private static String generateSecureSecret() {
        def random = new SecureRandom()
        def bytes = new byte[32] // 256 bits
        random.nextBytes(bytes)
        bytes.encodeHex().toString()
    }
}
