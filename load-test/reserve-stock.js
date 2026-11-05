import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// 시나리오 1 (명세 10.3): 대기열 없이 선점 API를 직접 호출해 재고 차감 방식별 성능을 비교한다.
// 준비: 드롭을 OPEN + 재고 초기화, 비교 대상 전략으로 기동(shoesmarket.stock.strategy),
//       토큰 검증 우회(shoesmarket.stock.token-check-enabled=false).
//
// 실행 예: k6 run -e BASE_URL=http://localhost:8080 -e DROP_ID=1 reserve-stock.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const DROP_ID = __ENV.DROP_ID || '1';

const soldOut = new Counter('sold_out');
const success = new Counter('reserve_success');

export const options = {
  scenarios: {
    rush: {
      executor: 'per-vu-iterations',
      vus: 5000,
      iterations: 1,
      maxDuration: '60s',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
  },
};

export default function () {
  // VU마다 고유 userId로 1인 1건 제한을 자연스럽게 만족시킨다.
  const userId = __VU;
  const res = http.post(`${BASE_URL}/api/v1/drops/${DROP_ID}/reservations`, null, {
    headers: { 'X-User-Id': String(userId) },
  });
  if (res.status === 201) success.add(1);
  if (res.status === 409) soldOut.add(1);
  check(res, {
    'no server error': (r) => r.status < 500,
  });
}
