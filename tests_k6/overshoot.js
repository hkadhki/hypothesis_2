import http from 'k6/http';
import { Counter } from 'k6/metrics';

export const options = {
    scenarios: {
        redis: {
            executor: 'constant-vus',
            vus: 200,
            duration: '10s',
            exec: 'testRedis',
        },
        envoy: {
            executor: 'constant-vus',
            vus: 200,
            duration: '10s',
            startTime: '15s',
            exec: 'testEnvoy',
        },
        dht: {
            executor: 'constant-vus',
            vus: 200,
            duration: '10s',
            startTime: '30s',
            exec: 'testDHT',
        },
    },
};


const redisAllowed = new Counter('redis_allowed');
const redisLimited = new Counter('redis_limited');


const envoyAllowed = new Counter('envoy_allowed');
const envoyLimited = new Counter('envoy_limited');

const dhtAllowed = new Counter('dht_allowed');
const dhtLimited = new Counter('dht_limited');


const REDIS_URL = 'http://localhost:8080/redis/test';
const ENVOY_URL = 'http://localhost:10000/dht/test';
const DHT_URL   = 'http://localhost:8080/dht/test';


export function testRedis() {
    runTest(REDIS_URL, redisAllowed, redisLimited);
}


export function testEnvoy() {
    runTest(ENVOY_URL, envoyAllowed, envoyLimited);
}


export function testDHT() {
    runTest(DHT_URL, dhtAllowed, dhtLimited);
}

function runTest(url, allowedCounter, limitedCounter) {
    const res = http.get(url, {
        headers: { 'X-User-Id': 'OVERSHOOT_USER' },
    });

    if (res.status >= 200 && res.status < 300) {
        allowedCounter.add(1);
    } else if (res.status === 429) {
        limitedCounter.add(1);
    }
}
