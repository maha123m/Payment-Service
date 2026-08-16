package payment.service

import grails.converters.JSON
import payment.service.exception.BusinessException

abstract class BaseApiController {

    protected void renderJson(int status, Map body) {
        response.status = status
        response.contentType = 'application/json'
        render(text: body as JSON, encoding: 'UTF-8')
    }

    protected void renderError(BusinessException exception) {
        renderJson(exception.httpStatus, [
                errorCode: exception.errorCode,
                error    : exception.message
        ])
    }

    protected void handleRequest(Closure action) {
        try {
            action.call()
        } catch (BusinessException exception) {
            renderError(exception)
        } catch (Exception exception) {
            log.error("Unexpected error", exception)
            renderJson(500, [
                    errorCode: "500",
                    error    : "Internal server error",
                    details  : exception.message
            ])
        }
    }
}
