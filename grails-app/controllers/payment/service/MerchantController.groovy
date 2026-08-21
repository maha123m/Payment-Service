package payment.service

import payment.service.api.MerchantResponse
import payment.service.commands.CreateMerchantCommand

class MerchantController extends BaseApiController {

    MerchantService merchantService

    def save(CreateMerchantCommand cmd) {
        handleRequest {
            if (!validateCommand(cmd)) {
                return
            }

            def merchant = merchantService.createMerchant(cmd)
            renderJson(201, MerchantResponse.toCreateResponse(merchant))
        }
    }
}

