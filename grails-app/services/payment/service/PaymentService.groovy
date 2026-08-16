package payment.service

import grails.gorm.transactions.Transactional
import payment.service.commands.CreatePaymentCommand
import payment.service.enums.PaymentStatus
import payment.service.exception.BusinessException
import payment.service.exception.ErrorCode
import payment.service.api.PaymentResponse
import java.text.SimpleDateFormat
import java.util.Calendar

@Transactional
class PaymentService {

    MerchantService merchantService

    PaymentTransaction processPayment(String apiKey, CreatePaymentCommand cmd) {
        def merchant = merchantService.getMerchantByApiKey(apiKey)

        if (!cmd.reference || cmd.amount == null || !cmd.currency) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_MISSING_FIELDS,
                    'Missing required fields: reference (unique payment ID), amount (greater than 0), and currency.'
            )
        }

        if (cmd.amount <= BigDecimal.ZERO) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_INVALID_AMOUNT, 
                    "Amount must be greater than 0. You provided: ${cmd.amount}"
            )
        }

        if (PaymentTransaction.findByReference(cmd.reference)) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_DUPLICATE_REFERENCE, 
                    "Payment reference '${cmd.reference}' already exists. Please use a unique reference for this payment."
            )
        }

        def payment = new PaymentTransaction(
                reference  : cmd.reference,
                amount     : cmd.amount,
                currency   : cmd.currency,
                description: cmd.description,
                merchant   : merchant,
                status     : PaymentStatus.PENDING
        )

        if (!payment.save(flush: true)) {
            def details = payment.errors.allErrors*.defaultMessage.join(', ')
            throw new BusinessException(ErrorCode.PAYMENT_VALIDATION_FAILED, "Payment validation failed: ${details}")
        }

        payment
    }

    @Transactional
    PaymentTransaction capturePayment(String apiKey, String reference) {
        def merchant = merchantService.getMerchantByApiKey(apiKey)
        def payment = findOwnedPayment(
                merchant,
                reference,
                ErrorCode.CAPTURE_PAYMENT_NOT_FOUND,
                ErrorCode.CAPTURE_NOT_OWNED
        )

        if (payment.status != PaymentStatus.PENDING) {
            throw new BusinessException(
                    ErrorCode.CAPTURE_INVALID_STATUS,
                    "Cannot capture payment. Current status is '${payment.status}'. Payment must be in PENDING status to be captured."
            )
        }

        payment.status = PaymentStatus.SUCCESS
        saveOrFail(payment, ErrorCode.CAPTURE_SAVE_FAILED)
    }

    @Transactional
    PaymentTransaction refundPayment(String apiKey, String reference) {
        def merchant = merchantService.getMerchantByApiKey(apiKey)
        def payment = findOwnedPayment(
                merchant,
                reference,
                ErrorCode.REFUND_PAYMENT_NOT_FOUND,
                ErrorCode.REFUND_NOT_OWNED
        )

        if (payment.status != PaymentStatus.SUCCESS) {
            throw new BusinessException(
                    ErrorCode.REFUND_INVALID_STATUS,
                    "Cannot refund payment. Current status is '${payment.status}'. Only SUCCESS payments can be refunded."
            )
        }

        payment.status = PaymentStatus.REFUNDED
        saveOrFail(payment, ErrorCode.REFUND_SAVE_FAILED)
    }

    PaymentTransaction getPayment(String apiKey, String reference) {
        def merchant = merchantService.getMerchantByApiKey(apiKey)
        findOwnedPayment(
                merchant,
                reference,
                ErrorCode.PAYMENT_NOT_FOUND,
                ErrorCode.PAYMENT_NOT_OWNED
        )
    }

    List<PaymentTransaction> listPayments(String apiKey, Map params) {
        def merchant = merchantService.getMerchantByApiKey(apiKey)

        PaymentStatus statusFilter = null
        if (params.status) {
            try {
                statusFilter = PaymentStatus.valueOf(params.status as String)
            } catch (IllegalArgumentException ignored) {
                throw new BusinessException(ErrorCode.LIST_INVALID_STATUS, 'Invalid status filter')
            }
        }

        Date fromDate = parseDate(params.fromDate, 'fromDate')
        Date toDate = parseDate(params.toDate, 'toDate')
        if (toDate) {
            // Add 1 day to include the entire toDate day
            Calendar cal = Calendar.getInstance()
            cal.setTime(toDate)
            cal.add(Calendar.DATE, 1)
            toDate = cal.getTime()
        }

        PaymentTransaction.createCriteria().list {
            eq('merchant', merchant)
            if (statusFilter) {
                eq('status', statusFilter)
            }
            if (fromDate) {
                ge('dateCreated', fromDate)
            }
            if (toDate) {
                lt('dateCreated', toDate)
            }
            order('dateCreated', 'desc')
        } as List<PaymentTransaction>
    }

    /**
     * Lists payments with pagination support.
     * @param apiKey the merchant's API key
     * @param params query parameters including max, offset, and filter parameters
     * @return a paginated map with total, count, payments, and pagination metadata
     */
    Map listPaymentsPaginated(String apiKey, Map params) {
        def merchant = merchantService.getMerchantByApiKey(apiKey)

        PaymentStatus statusFilter = null
        if (params.status) {
            try {
                statusFilter = PaymentStatus.valueOf(params.status as String)
            } catch (IllegalArgumentException ignored) {
                throw new BusinessException(ErrorCode.LIST_INVALID_STATUS, 'Invalid status filter')
            }
        }

        Date fromDate = parseDate(params.fromDate, 'fromDate')
        Date toDate = parseDate(params.toDate, 'toDate')
        if (toDate) {
            // Add 1 day to include the entire toDate day
            Calendar cal = Calendar.getInstance()
            cal.setTime(toDate)
            cal.add(Calendar.DATE, 1)
            toDate = cal.getTime()
        }

        // Get total count without pagination
        int totalCount = PaymentTransaction.createCriteria().count {
            eq('merchant', merchant)
            if (statusFilter) {
                eq('status', statusFilter)
            }
            if (fromDate) {
                ge('dateCreated', fromDate)
            }
            if (toDate) {
                lt('dateCreated', toDate)
            }
        }

        // Parse and validate pagination parameters
        int max = (params.max as Integer) ?: 20
        int offset = (params.offset as Integer) ?: 0

        if (max < 1 || max > 500) {
            throw new BusinessException(ErrorCode.LIST_INVALID_PARAM, 'max must be between 1 and 500')
        }
        if (offset < 0) {
            throw new BusinessException(ErrorCode.LIST_INVALID_PARAM, 'offset must be >= 0')
        }

        // Get paginated results
        List<PaymentTransaction> payments = PaymentTransaction.createCriteria().list(
            max: max,
            offset: offset
        ) {
            eq('merchant', merchant)
            if (statusFilter) {
                eq('status', statusFilter)
            }
            if (fromDate) {
                ge('dateCreated', fromDate)
            }
            if (toDate) {
                lt('dateCreated', toDate)
            }
            order('dateCreated', 'desc')
        } as List<PaymentTransaction>

        int nextOffset = offset + max
        boolean hasNext = nextOffset < totalCount

        [
            total: totalCount,
            count: payments.size(),
            max: max,
            offset: offset,
            hasNext: hasNext,
            payments: payments.collect { PaymentResponse.toSummary(it) }
        ]
    }

    private PaymentTransaction findOwnedPayment(
            Merchant merchant,
            String reference,
            ErrorCode notFoundError,
            ErrorCode notOwnedError
    ) {
        def payment = PaymentTransaction.findByReference(reference)
        if (!payment) {
            throw new BusinessException(notFoundError, 'Payment not found')
        }

        if (payment.merchant.id != merchant.id) {
            throw new BusinessException(notOwnedError, 'Payment does not belong to this merchant')
        }

        payment
    }

    private PaymentTransaction saveOrFail(PaymentTransaction payment, ErrorCode errorCode) {
        if (!payment.save(flush: true)) {
            def details = payment.errors.allErrors*.defaultMessage.join(', ')
            throw new BusinessException(errorCode, "Failed to update payment: ${details}")
        }

        payment
    }

    private Date parseDate(value, String fieldName) {
        if (!value) {
            return null
        }

        try {
            def dateStr = value as String
            // Use SimpleDateFormat to ensure consistent parsing
            def sdf = new java.text.SimpleDateFormat('yyyy-MM-dd')
            sdf.setLenient(false)  // Strict parsing - rejects invalid dates like 2026-02-30
            return sdf.parse(dateStr)
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.LIST_INVALID_DATE,
                    "Invalid ${fieldName} format, expected yyyy-MM-dd. Error: ${e.message}"
            )
        }
    }
}
