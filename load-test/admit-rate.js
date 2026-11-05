import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// 시나리오 3 (명세 10.3): admit-per-second(초당 입장 인원)를 50/100/300으로 바꿔가며
// 입장 후 선점까지의 응답 시간을 비교한다. 서버의 shoesmarket.queue.admit-per-second를 바꿔 재기동하며 반복 실행한다.
// 실행 예: k6 run -e BASE_URL=http://localhost:8080 -e DROP_ID=1 admit-rate.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const DROP_ID = __ENV.DROP_ID || '1';

const timeToAdmit = new Trend('time_to_admit_ms');

export const options = {
  scenarios: {
    steady: {
      executor: 'constant-vus',
      vus: 1000,
      duration: '60s',
    },
  },
};

export default function () {
  const userId = __VU * 100000 + __ITER;
  const headers = { 'X-User-Id': String(userId) };

  const t0 = Date.now();
  http.post(`${BASE_URL}/api/v1/drops/${DROP_ID}/queue`, null, { headers });

  for (let i = 0; i < 30; i++) {
    const me = http.get(`${BASE_URL}/api/v1/drops/${DROP_ID}/queue/me`, { headers });
    if (me.status === 200 && me.json('data.status') === 'ADMITTED') {
      timeToAdmit.add(Date.now() - t0);
      const res = http.post(`${BASE_URL}/api/v1/drops/${DROP_ID}/reservations`, null, { headers });
      check(res, { 'reserve handled': (r) => r.status < 500 });
      break;
    }
    sleep(1);
  }
}
