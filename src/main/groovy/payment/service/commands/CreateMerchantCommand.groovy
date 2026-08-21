package payment.service.commands

import grails.validation.Validateable

/**
 * Validates the request payload before it reaches MerchantService.
 */
class CreateMerchantCommand implements Validateable {
    String name
    String email

    static constraints = {
        name blank: false, nullable: false
        email blank: false, nullable: false, email: true
    }
}
