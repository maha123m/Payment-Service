package payment.service

import payment.service.commands.CreatePaymentCommand
import payment.service.exception.BusinessException
import payment.service.exception.ErrorCode

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest


class PaymentRequestSignatureService {

    private static final String HMAC_ALGORITHM = 'HmacSHA256'

    MerchantService merchantService

    void verifyCreatePayment(String apiKey, String signature, String requestBody) {
        verifySignature(apiKey, signature, requestBody)
    }

    void verifyReferenceAction(String apiKey, String signature) {
        verifySignature(apiKey, signature, '')
    }

    private void verifySignature(String apiKey, String signature, String requestBody) {
        if (!apiKey) {
            throw new BusinessException(ErrorCode.API_KEY_MISSING, 'Missing X-API-KEY header.')
        }
        if (!signature) {
            throw new BusinessException(ErrorCode.SIGNATURE_MISSING, 'Missing X-SIGNATURE header.')
        }
        if (requestBody == null) {
            throw new BusinessException(ErrorCode.SIGNATURE_INVALID, 'Invalid request signature.')
        }

        def merchant = merchantService.getMerchantByApiKey(apiKey)
        if (!merchant.secretKey) {
            throw new BusinessException(ErrorCode.SIGNATURE_INVALID, 'Invalid request signature.')
        }

        String expectedSignature = calculateHmac(merchant.secretKey, requestBody)

        if (!MessageDigest.isEqual(
                expectedSignature.toLowerCase(Locale.ROOT).getBytes('US-ASCII'),
                signature.toLowerCase(Locale.ROOT).getBytes('US-ASCII')
        )) {
            throw new BusinessException(ErrorCode.SIGNATURE_INVALID, 'Invalid request signature.')
        }
    }

    private static String calculateHmac(String secretKey, String data) {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(new SecretKeySpec(secretKey.getBytes('UTF-8'), HMAC_ALGORITHM))
        mac.doFinal(data.getBytes('UTF-8')).encodeHex().toString()
    }
}
