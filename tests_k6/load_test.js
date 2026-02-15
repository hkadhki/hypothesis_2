import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

export const options = {
    scenarios: {
        redis: {
            executor: 'constant-vus',
            vus: 200,
            duration: '30s',
            exec: 'testRedis',
        },
        envoy: {
            executor: 'constant-vus',
            vus: 200,
            duration: '30s',
            startTime: '35s',
            exec: 'testEnvoy',
        },
        dht: {
            executor: 'constant-vus',
            vus: 200,
            duration: '30s',
            startTime: '70s',
            exec: 'testDHT',
        },
    },
};


const redisAllowed = new Counter('redis_allowed');
const redisLimited = new Counter('redis_limited');
const redisErrors  = new Counter('redis_errors');
const redisLatency = new Trend('redis_latency');


const envoyAllowed = new Counter('envoy_allowed');
const envoyLimited = new Counter('envoy_limited');
const envoyErrors  = new Counter('envoy_errors');
const envoyLatency = new Trend('envoy_latency');


const dhtAllowed = new Counter('dht_allowed');
const dhtLimited = new Counter('dht_limited');
const dhtErrors  = new Counter('dht_errors');
const dhtLatency = new Trend('dht_latency');


const REDIS_URL = 'http://localhost:8080/redis/test';
const ENVOY_URL = 'http://localhost:10000/dht/test';
const DHT_URL   = 'http://localhost:8080/dht/test';


export function testRedis() {
    runTest(REDIS_URL, redisAllowed, redisLimited, redisErrors, redisLatency);
}


export function testEnvoy() {
    runTest(ENVOY_URL, envoyAllowed, envoyLimited, envoyErrors, envoyLatency);
}


export function testDHT() {
    runTest(DHT_URL, dhtAllowed, dhtLimited, dhtErrors, dhtLatency);
}


function runTest(url, allowed, limited, errors, latencyTrend) {
    const userId = 'user-' + Math.floor(Math.random() * 10000);

    const res = http.get(url, {
        headers: { 'X-User-Id': userId },
    });

    check(res, {
        'status is 2xx or 429': (r) =>
            (r.status >= 200 && r.status < 300) || r.status === 429,
    });

    if (res.status >= 200 && res.status < 300) {
        allowed.add(1);
        latencyTrend.add(res.timings.duration);
    } else if (res.status === 429) {
        limited.add(1);
    } else {
        errors.add(1);
    }
}
