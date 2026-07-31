import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 10,
    iterations: 1000,
};

const duplicateEventId = __ENV.DUP_EVENT_ID || 'evt-duplicate-load-001';

export default function () {
    const body = JSON.stringify({
        externalEventId: duplicateEventId,
        eventType: 'CUSTOMER_UPDATED',
        payloadJson: JSON.stringify({
            customerId: 'CRM-101',
            email: 'duplicate-test@email.com'
        })
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.post(
        'http://localhost:8080/api/integrations/1/webhooks',
        body,
        params
    );

    check(response, {
        'webhook accepted': (res) => res.status === 200 || res.status === 201,
        'response is received or duplicate': (res) => {
            const json = res.json();
            return json.status === 'RECEIVED' || json.status === 'DUPLICATE';
        },
    });
}
