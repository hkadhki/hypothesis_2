import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

export const options = {
    vus: 200,
    duration: '30s',
};


const allowed = new Counter('allowed');              // успешно прошли (2xx)
const limited = new Counter('limited');              // отрезаны лимитером (429)
const errors  = new Counter('errors');               // реальные ошибки (5xx, timeout)
const allowedLatency = new Trend('allowed_latency'); // latency только для 2xx


const TARGET = __ENV.TARGET || 'http://localhost:8080/dht/test';

export default function () {
    const userId = 'user-' + Math.floor(Math.random() * 10000);

    const res = http.get(TARGET, {
        headers: { 'X-User-Id': userId },
    });

    check(res, {
        'status is 2xx or 429': (r) =>
            (r.status >= 200 && r.status < 300) || r.status === 429,
    });

    if (res.status >= 200 && res.status < 300) {
        allowed.add(1);
        allowedLatency.add(res.timings.duration);
    } else if (res.status === 429) {
        limited.add(1);
    } else {
        errors.add(1);
    }
}
