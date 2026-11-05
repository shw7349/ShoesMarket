import http from 'k6/http';
import { check, sleep } from 'k6';

// 시나리오 2 (명세 10.3): 대기열 진입 → 순번 폴링 → 입장(토큰 발급) → 선점까지 전체 흐름.
// 실행 예: k6 run -e BASE_URL=http://localhost:8080 -e DROP_ID=1 queue-flow.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const DROP_ID = __ENV.DROP_ID || '1';

export const options = {
  scenarios: {
    flood: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 2000 },
        { duration: '30s', target: 2000 },
        { duration: '10s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1500'],
  },
};

export default function () {
  const userId = __VU * 100000 + __ITER;
  const headers = { 'X-User-Id': String(userId) };

  http.post(`${BASE_URL}/api/v1/drops/${DROP_ID}/queue`, null, { headers });

  // 입장할 때까지 순번을 폴링한다(최대 20회).
  let admitted = false;
  for (let i = 0; i < 20; i++) {
    const me = http.get(`${BASE_URL}/api/v1/drops/${DROP_ID}/queue/me`, { headers });
    if (me.status === 200 && me.json('data.status') === 'ADMITTED') {
      admitted = true;
      break;
    }
    sleep(1);
  }

  if (admitted) {
    const res = http.post(`${BASE_URL}/api/v1/drops/${DROP_ID}/reservations`, null, { headers });
    check(res, { 'reserve handled': (r) => r.status < 500 });
  }
}
