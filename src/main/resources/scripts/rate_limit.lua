local current = redis.call('INCR', KEYS[1])
local ttl = redis.call('TTL', KEYS[1])

if current == 1 or ttl < 0 then
    redis.call('EXPIRE', KEYS[1], ARGV[1])
    ttl = tonumber(ARGV[1])
end

local limit = tonumber(ARGV[2])
local remaining = limit - current

if remaining < 0 then
    remaining = 0
end

local allowed = 0

if current <= limit then
    allowed = 1
end

return {
    allowed,
    remaining,
    ttl
}