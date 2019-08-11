package cn.foperate.base.redis

import io.vertx.codegen.annotations.Fluent
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.redis.*
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions

// 本类的作用在于提供一个多线程安全访问Redis，及自动维护数据超时的工具类
class RedisDelegate(var vertx: Vertx, val options: RedisOptions) {

    private var redis: ThreadLocal<RedisClient?> = ThreadLocal()
    private var expire = 300L // 正常使用情况下，应该设置一个非0的预期过期值

    @Fluent
    fun setExpire(expire: Long): RedisDelegate {
        this.expire = expire
        return this
    }

    /*@Fluent
    fun init(vertx: Vertx): RedisDelegate {
      this.vertx = vertx
      return this
    }*/

    // 这个函数确保不同线程使用的是不同的RedisClient实例。
    private fun getClient():RedisClient {
        if (redis.get()==null) {
            val client = RedisClient.create(vertx, options)
            redis.set(client)
        }
        return redis.get()!!
    }

    suspend fun getAwait(id:String): String? {
        return try {
            val api = getClient()
            val response = api.getAwait(id)
            if (expire!=0L) {
                // 这里的一个副作用是，如果没有设置缺省值，而之前又设置了一个会过期的值，会导致这个值被提前无预期地删除
                api.expire(id, expire){} // 不用专门处理重置超时的动作是否完成
            }
            response
        } catch (e:Exception) { // 如果服务器连接不上，会返回这个异常
            log.error(e)
            null
        }
    }

    suspend fun getAwait(id:String, seconds:Long): String? {
        return try {
            val api = getClient()
            val response = api.getAwait(id)
            return if (response==null) null else {
                api.expire(id, seconds){}
                response
            }
        } catch (e:Exception) {
            null
        }
    }

    suspend fun getBinaryAwait(key: String) = getClient().getBinaryAwait(key)

    suspend fun setAwait(id:String, value: String) {
        val api = getClient()
        if (expire==0L) {
            api.setAwait(id, value)
        } else {
            api.setexAwait(id, expire, value)
        }
    }

    suspend fun setAwait(id: String, value: String, seconds: Long) = getClient().setexAwait(id, seconds, value)

    suspend fun setBinaryAwait(key: String, value: Buffer) = getClient().setBinaryAwait(key, value)
    suspend fun expireAwait(key:String, seconds: Long) = getClient().expireAwait(key, seconds)
    fun expireLater(key: String, seconds: Long) = getClient().expire(key, seconds){}

    suspend fun delAwait(id:String) = getClient().delAwait(id)
    fun delLater(id:String) = getClient().del(id){}

    suspend fun countAwait() = getClient().dbsizeAwait()
    suspend fun clearAwait() = getClient().flushdbAwait()

    companion object {
        private val log = LoggerFactory.getLogger(RedisDelegate::class.java)
    }
}
