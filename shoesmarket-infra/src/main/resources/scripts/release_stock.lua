-- KEYS[1] = drop:{id}:stock
-- KEYS[2] = drop:{id}:buyers
-- ARGV[1] = userId
-- SREM 결과로 중복 반환을 막는다. 반환 성공 시 증가된 재고, 아니면 -1
if redis.call('SREM', KEYS[2], ARGV[1]) == 1 then
    return redis.call('INCR', KEYS[1])
end
return -1
