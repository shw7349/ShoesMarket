# 👟 ShoesMarket

> 수만 명이 동시에 몰리는 한정판 스니커즈 드롭 상황을 가정한 선착순 구매 시스템

정해진 시각에 한정 수량을 판매할 때 생기는 문제들을 다룹니다.

- **트래픽 제어**: Redis Sorted Set 기반 대기열과 입장 토큰
- **초과판매 방지**: Lua 스크립트로 1인 제한, 재고 확인, 차감을 원자적으로 처리
- **결제 정합성**: 멱등성 키, 선점 만료 처리, PG 대사 배치
- **검증**: k6 부하 테스트로 재고 차감 방식별 성능 비교

## Tech Stack
Kotlin · Spring Boot · Spring Data JPA · Redis · MySQL · Spring Batch · Kotest · k6
