-- KEYS[1] = drop:{id}:stock
-- KEYS[2] = drop:{id}:buyers
-- KEYS[3] = drop:{id}:token:{userId}
-- ARGV[1] = userId
-- ARGV[2] = tokenRequired ("1" 이면 입장 토큰 필요)
-- return: 남은 재고(>=0) | -1 품절 | -2 이미 구매 | -3 토큰 없음 | -4 드롭 미오픈(재고 미초기화)

if redis.call('EXISTS', KEYS[1]) == 0 then
    return -4
end

if ARGV[2] == '1' then
    if redis.call('EXISTS', KEYS[3]) == 0 then
        return -3
    end
end

if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -2
end

local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
if stock <= 0 then
    return -1
end

redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
if ARGV[2] == '1' then
    redis.call('DEL', KEYS[3])
end
return stock - 1
