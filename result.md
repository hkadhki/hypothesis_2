### Запуск тестов

### P95 latency

cd tests_k6

DHT
k6 run -e TARGET=http://localhost:8080/dht/test --summary-export=results-dht.json load_test.js

Redis
k6 run -e TARGET=http://localhost:8080/redis/test --summary-export=results-redis.json load_test.js

Envoy
k6 run -e TARGET=http://localhost:10000/dht/test --summary-export=results-envoy.json load_test.js

### Overshoot rate

DHT
k6 run -e TARGET=http://localhost:8080/dht/test --summary-export=overshoot-dht.json overshoot.js

Redis
k6 run -e TARGET=http://localhost:8080/redis/test --summary-export=overshoot-redis.json overshoot.js

Envoy
k6 run -e TARGET=http://localhost:10000/dht/test --summary-export=overshoot-envoy.json overshoot.js

#### overshoot = (allowed - theoretical_max) / theoretical_max



### Fault tolerance

k6 run -e TARGET=http://localhost:8080/dht/test --summary-export=fault-dht.json load_test.js

Во время тестов отключаем ноду

docker stop limiter-3


### Rebalancing

для проверки перераспределение ключей необходимо создавать новые ноды во время работы 


Результат

| Показатель                          | DHT                        | Redis                               | Envoy                       |
|-------------------------------------|----------------------------|-------------------------------------|-----------------------------|
| **RPS (общий тест)**                | 6 316                      | 6 499                               | **26 665**                  |
| **Средняя задержка (avg latency)**  | 31.5 ms                    | 30.6 ms                             | **7.38 ms**                 |
| **P95 задержка (latency)**          | 51.9 ms                    | 49.7 ms                             | **14.2 ms**                 |
| **Overshoot (превышение лимита)**   | **0%**                     | **0%**                              | **~81%**                    |
| **Устойчивость к падению узла**     | Высокая (≈ –7% RPS)        | Зависит от отказоустойчивости Redis | N/A (локальный лимитер)     |
| **Перераспределение ключей (DHT)**  | ✔ Consistent Hashing       | ✖ (централизованное хранилище)     | ✖                           |

