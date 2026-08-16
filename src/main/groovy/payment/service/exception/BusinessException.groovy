package payment.service.exception

class BusinessException extends RuntimeException {

    final String errorCode
    final int httpStatus

    BusinessException(ErrorCode errorCode, String message) {
        super(message)
        this.errorCode = errorCode.code
        this.httpStatus = errorCode.httpStatus
    }

    BusinessException(String errorCode, String message, int httpStatus = 400) {
        super(message)
        this.errorCode = errorCode
        this.httpStatus = httpStatus
    }
}
