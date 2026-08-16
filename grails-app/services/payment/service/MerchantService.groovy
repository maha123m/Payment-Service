package payment.service

import grails.gorm.transactions.Transactional
import payment.service.commands.CreateMerchantCommand
import payment.service.exception.BusinessException
import payment.service.exception.ErrorCode

@Transactional
class MerchantService {

    Merchant createMerchant(CreateMerchantCommand cmd) {
        def merchant = new Merchant(name: cmd.name, email: cmd.email)

        if (!merchant.save(flush: true)) {
            def errorDetails = merchant.errors.allErrors.collect { error ->
                if (error.field == 'email' && error.code == 'unique') {
                    return "Email '${cmd.email}' is already in use. Please use a different email address."
                } else if (error.field == 'email' && error.code == 'email.invalid') {
                    return "Email '${cmd.email}' is not a valid email format."
                } else if (error.field == 'email') {
                    return "Email is required and must be in valid format (e.g., user@example.com)."
                } else if (error.field == 'name') {
                    return "Merchant name is required and cannot be empty."
                } else {
                    return error.defaultMessage
                }
            }.join('; ')
            
            throw new BusinessException(ErrorCode.MERCHANT_VALIDATION_FAILED, errorDetails)
        }

        merchant
    }

    Merchant getMerchantByApiKey(String apiKey) {
        if (!apiKey) {
            throw new BusinessException(ErrorCode.API_KEY_MISSING, 'Missing X-API-KEY header')
        }

        def merchant = Merchant.findByApiKey(apiKey)
        if (!merchant) {
            throw new BusinessException(ErrorCode.API_KEY_INVALID, 'Invalid API key')
        }

        if (!merchant.active) {
            throw new BusinessException(ErrorCode.MERCHANT_INACTIVE, 'Merchant is inactive')
        }

        merchant
    }
}
