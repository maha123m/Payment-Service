class UrlMappings {

    static mappings = {
        group('/api') {
            '/merchants'(controller: 'merchant', action: 'save', method: 'POST')

            '/payments'(controller: 'payment') {
                action = [POST: 'save', GET: 'index']
            }
            "/payments/$reference/capture"(controller: 'payment', action: 'capture', method: 'POST')
            "/payments/$reference/refund"(controller: 'payment', action: 'refund', method: 'POST')
            "/payments/$reference"(controller: 'payment', action: 'show', method: 'GET')
        }

        '/'(controller: 'application', action: 'index')
        '500'(controller: 'error')
        '404'(controller: 'error', action: 'notFound')
    }
}
