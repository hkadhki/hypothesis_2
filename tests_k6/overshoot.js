import http from 'k6/http';
import { Counter } from 'k6/metrics';

export const options = {
    vus: 200,
    duration: '10s',
};

const allowed = new Counter('allowed');
const limited = new Counter('limited');

const TARGET = __ENV.TARGET || 'http://localhost:8080/dht/test';

export default function () {
    const res = http.get(TARGET, {
        headers: { 'X-User-Id': 'OVERSHOOT_USER' },
    });

    if (res.status >= 200 && res.status < 300) {
        allowed.add(1);
    } else if (res.status === 429) {
        limited.add(1);
    }
}
