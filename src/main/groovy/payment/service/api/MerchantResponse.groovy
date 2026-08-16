package payment.service.api

import payment.service.Merchant

class MerchantResponse {

    static Map toCreateResponse(Merchant merchant) {
        [
                id    : merchant.id,
                name  : merchant.name,
                email : merchant.email,
                apiKey: merchant.apiKey
        ]
    }

    static Map toDetailResponse(Merchant merchant) {
        [
                id         : merchant.id,
                name       : merchant.name,
                email      : merchant.email,
                apiKey     : merchant.apiKey,
                active     : merchant.active,
                dateCreated: merchant.dateCreated,
                lastUpdated: merchant.lastUpdated
        ]
    }
}
