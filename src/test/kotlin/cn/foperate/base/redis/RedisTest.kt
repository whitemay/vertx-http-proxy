package cn.foperate.base.redis

import io.vertx.core.Vertx
import io.vertx.kotlin.redis.redisOptionsOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RedisTest {
    @Test
    fun testRedisDelegate() {
        val vertx = Vertx.vertx()
        val options = redisOptionsOf(
                auth = "1234567890",
                tcpKeepAlive = true
        )
        val redis = RedisDelegate(vertx, options)
        runBlocking {
            redis.setAwait("my", "Aston", 10000)
            redis.expireAwait("my", 10000)
            var str = redis.getAwait("my")
            Assertions.assertEquals(str, "Aston")
            val info = redis.countAwait()
            println(info)

            redis.delAwait("my")
            str = redis.getAwait("my")
            Assertions.assertNull(str)
        }
    }
}
