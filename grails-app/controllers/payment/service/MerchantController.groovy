package payment.service

import payment.service.api.MerchantResponse
import payment.service.commands.CreateMerchantCommand

class MerchantController extends BaseApiController {

    MerchantService merchantService

    def save() {
        handleRequest {
            def cmd = new CreateMerchantCommand(request.JSON ?: [:])
            def merchant = merchantService.createMerchant(cmd)
            renderJson(201, MerchantResponse.toCreateResponse(merchant))
        }
    }
}

