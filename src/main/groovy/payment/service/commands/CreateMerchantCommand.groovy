package payment.service.commands

/**
 * Command object for creating a merchant
 */
class CreateMerchantCommand {
    String name
    String email

    static constraints = {
        name blank: false, nullable: false
        email blank: false, nullable: false, email: true, unique: true
    }

    CreateMerchantCommand() {}

    CreateMerchantCommand(Map data) {
        this.name = data?.name
        this.email = data?.email
    }
}
