package payment.service.exception

enum ErrorCode {

    MERCHANT_VALIDATION_FAILED('10', 400),
    MERCHANT_NOT_FOUND('11', 404),

    PAYMENT_MISSING_FIELDS('20', 400),
    PAYMENT_INVALID_AMOUNT('21', 400),
    PAYMENT_DUPLICATE_REFERENCE('22', 400),
    PAYMENT_VALIDATION_FAILED('23', 400),

    API_KEY_MISSING('30', 401),
    API_KEY_INVALID('31', 401),
    MERCHANT_INACTIVE('32', 403),

    CAPTURE_PAYMENT_NOT_FOUND('40', 404),
    CAPTURE_NOT_OWNED('41', 403),
    CAPTURE_INVALID_STATUS('42', 400),
    CAPTURE_SAVE_FAILED('43', 500),

    REFUND_PAYMENT_NOT_FOUND('50', 404),
    REFUND_NOT_OWNED('51', 403),
    REFUND_INVALID_STATUS('52', 400),
    REFUND_SAVE_FAILED('53', 500),

    PAYMENT_NOT_FOUND('60', 404),
    PAYMENT_NOT_OWNED('61', 403),

    LIST_INVALID_STATUS('70', 400),
    LIST_INVALID_DATE('71', 400),
    LIST_INVALID_PARAM('72', 400),

    INTERNAL_ERROR('99', 500)

    final String code
    final int httpStatus

    ErrorCode(String code, int httpStatus) {
        this.code = code
        this.httpStatus = httpStatus
    }
}
