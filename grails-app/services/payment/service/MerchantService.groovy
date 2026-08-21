package payment.service

import grails.gorm.transactions.Transactional
import payment.service.commands.CreateMerchantCommand
import payment.service.exception.BusinessException
import payment.service.exception.ErrorCode

@Transactional
class MerchantService {

    Merchant createMerchant(CreateMerchantCommand cmd) {
        if (Merchant.findByEmail(cmd.email)) {
            throw new BusinessException(
                    ErrorCode.MERCHANT_VALIDATION_FAILED,
                    "Email '${cmd.email}' is already in use. Please use a different email address."
            )
        }

        def merchant = new Merchant(name: cmd.name, email: cmd.email)

        if (!merchant.save(flush: true)) {
            // The domain constraint remains the final safeguard against a
            // concurrent request creating the same email address.
            throw new BusinessException(
                    ErrorCode.MERCHANT_VALIDATION_FAILED,
                    'Merchant could not be created. Please try again.'
            )
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
