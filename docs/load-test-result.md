# 부하 테스트 결과

k6로 재고 차감 방식과 대기열 흐름을 측정한다. 스크립트는 `load-test/`에 있다.

## 실행 방법

```bash
docker compose up -d
./gradlew :shoesmarket-api:bootRun --args='--spring.profiles.active=local'

# 시나리오 1: 재고 차감 방식별 비교 (전략을 바꿔가며 재기동)
#   전략: shoesmarket.stock.strategy = LUA | PESSIMISTIC | OPTIMISTIC | REDISSON
#   선점 직접 호출이므로 토큰 검증을 끈다: shoesmarket.stock.token-check-enabled=false
k6 run -e BASE_URL=http://localhost:8080 -e DROP_ID=1 load-test/reserve-stock.js

# 시나리오 2: 대기열 전체 흐름
k6 run -e BASE_URL=http://localhost:8080 -e DROP_ID=1 load-test/queue-flow.js

# 시나리오 3: admit-per-second(50/100/300) 변경하며 반복
k6 run -e BASE_URL=http://localhost:8080 -e DROP_ID=1 load-test/admit-rate.js
```

## 측정 항목 (명세 10.2)

- TPS, p95 / p99 응답 시간
- 초과판매 발생 여부(성공 건수 > 재고면 실패)
- DB 커넥션 풀 대기 시간(HikariCP 메트릭)

## 재고 차감 방식 비교

방식별 특성은 다음과 같다. 수치는 각자 환경에서 위 스크립트를 돌려 채운다(아래 표의 수치는 측정 전 비워 둠).

| 방식 | 상호 배제 수단 | 강점 | 약점 | 초과판매 | TPS | p95(ms) | p99(ms) |
|---|---|---|---|---|---|---|---|
| Lua (기본) | Redis 단일 스레드 원자 실행 | 락 왕복 없음, 경합에 강함 | Redis 의존, 스크립트 관리 | 0 | — | — | — |
| Pessimistic | DB `SELECT ... FOR UPDATE` | 구현 단순, 정확 | 재고 행 직렬화로 처리량 낮음 | 0 | — | — | — |
| Optimistic | `@Version` + 재시도 | 락 없음 | 경합 심하면 재시도 폭증 | 0 | — | — | — |
| Redisson | 분산 락 | 다중 인스턴스 상호 배제 | 락 획득/해제 왕복 비용 | 0 | — | — | — |

> 측정 전이라 TPS/p95/p99는 비워 두었다. 네 방식 모두 초과판매가 0이어야 하고(정확성), 처리량은 Lua > Optimistic(저경합) > Pessimistic ≈ Redisson 순으로 나오는 것이 가설이다. 실제 수치로 이 가설을 검증한다.

## 대기열 / admit-per-second

| admit-per-second | 입장까지 평균(ms) | 선점 p95(ms) |
|---|---|---|
| 50 | — | — |
| 100 | — | — |
| 300 | — | — |

입장 속도를 올리면 대기 시간은 줄지만 선점/결제 경로의 순간 부하가 커진다. 트레이드오프를 수치로 확인하고 적정값을 고른다.
