import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 10,
    iterations: 1000,
};

export default function () {
    const eventId = `evt-load-${__VU}-${__ITER}-${Date.now()}`;

    const body = JSON.stringify({
        externalEventId: eventId,
        eventType: 'CUSTOMER_UPDATED',
        payloadJson: JSON.stringify({
            customerId: 'CRM-101',
            email: `${eventId}@test.com`
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
    });
}
