package com.shoesmarket.infra.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.scripting.support.ResourceScriptSource

@Configuration
class RedisConfig {
    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate = StringRedisTemplate(connectionFactory)

    @Bean
    fun reserveStockScript(): RedisScript<Long> {
        val script = DefaultRedisScript<Long>()
        script.setScriptSource(ResourceScriptSource(ClassPathResource("scripts/reserve_stock.lua")))
        script.resultType = Long::class.java
        return script
    }

    @Bean
    fun releaseStockScript(): RedisScript<Long> {
        val script = DefaultRedisScript<Long>()
        script.setScriptSource(ResourceScriptSource(ClassPathResource("scripts/release_stock.lua")))
        script.resultType = Long::class.java
        return script
    }
}
