/******
 * Redis used as a session in Vert.x / Kotlin corountines.
 * It used default Session Impl in Vert.x as Session, and put in Redis as binary.
 */
package cn.foperate.base.session

import cn.foperate.base.redis.RedisDelegate
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.auth.PRNG
import io.vertx.ext.web.Session
import io.vertx.ext.web.sstore.SessionStore
import io.vertx.ext.web.sstore.impl.SharedDataSessionImpl
import io.vertx.kotlin.redis.redisOptionsOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RedisSessionStoreImpl(override val coroutineContext: CoroutineContext) :RedisSessionStore, CoroutineScope {
    private var vertx: Vertx? = null
    private var random: PRNG? = null
    private var redis: RedisDelegate? =null
    private var retryTimeout:Long = DEFAULT_RETRY_TIMEOUT

    override fun delete(id: String, resultHandler: Handler<AsyncResult<Void>>) {
        // 下面的做法是确保handler重新回到原来的线程环境执行
        val future = Promise.promise<Void>()
        future.future().setHandler(resultHandler)
        launch {
            try {
                redis!!.delAwait(id)
                future.complete()
            } catch (e:Exception) {
                future.fail(e)
            }
        }
    }

    override fun clear(resultHandler: Handler<AsyncResult<Void>>?) {
        val future = Promise.promise<Void>()
        future.future().setHandler(resultHandler)
        launch {
            try {
                redis!!.clearAwait()
                future.complete()
            } catch(e:Exception) {
                future.fail(e)
            }
        }
    }

    override fun put(session: Session, resultHandler: Handler<AsyncResult<Void>>?) {
        val future = Promise.promise<Void>()
        future.future().setHandler(resultHandler)
        launch {
            session as SharedDataSessionImpl
            val buffer = Buffer.buffer()
            session.writeToBuffer(buffer)
            try {
                redis!!.setBinaryAwait(session.id(), buffer)
                if (session.timeout()!=0L) {
                    redis!!.expireAwait(session.id(), session.timeout())
                }
                future.complete()
            } catch (e:java.lang.Exception) {
                future.fail(e)
            }
        }
    }

    override fun retryTimeout() = this.retryTimeout

    override fun size(resultHandler: Handler<AsyncResult<Int>>?) {
        val future = Promise.promise<Int>()
        future.future().setHandler(resultHandler)
        launch {
            try {
                val count = redis!!.countAwait()
                future.complete(count.toInt())
            } catch (e: Exception) {
                future.fail(e)
            }
        }
    }

    override fun get(id: String, resultHandler: Handler<AsyncResult<Session?>>?) {
        val future = Promise.promise<Session>()
        future.future().setHandler(resultHandler)
        launch {
            try {
                val buffer = redis!!.getBinaryAwait(id)
                val session = SharedDataSessionImpl(random)
                if (buffer!=null) {
                    session.readFromBuffer(0, buffer)
                    if (session.id()!=id) {
                        redis!!.delLater(id)
                        future.complete(null)
                    }
                    if (session.timeout()!=0L) {
                        redis!!.expireLater(id, session.timeout())
                    }
                    future.complete(session)
                } else {
                    future.complete(null)
                }
            } catch (e:Exception) {
                future.fail(e)
            }
        }

    }

    override fun init(vertx: Vertx, options: JsonObject?): SessionStore {
        this.vertx = vertx
        this.random = PRNG(vertx)
        val redisOptions = redisOptionsOf(
                auth = "1234567890",
                select = 0
        )

        options?.let {
            this.retryTimeout = options.getLong("retryTimeout", DEFAULT_RETRY_TIMEOUT)
            redisOptions.address = options.getString("host", "127.0.0.1")
            redisOptions.port = options.getInteger("port", 6349)
        }
        this.redis = RedisDelegate(vertx, redisOptions)

        return this
    }

    override fun createSession(timeout: Long): Session {
        val session = SharedDataSessionImpl(random, timeout, SessionStore.DEFAULT_SESSIONID_LENGTH)
        // Session本身有meta信息。所以不管有没有存过值，只要是建了Session，就会保存，直到这个Session过期或者被删除
        this.put(session){}
        return session
    }

    override fun createSession(timeout: Long, length: Int): Session {
        val session = SharedDataSessionImpl(random, timeout, length)
        this.put(session){}
        return session
    }

    override fun close() {
        random?.close()
    }

    companion object {
        /**
         * Default retry time out, in ms, for a session not found in this store.
         */
        private const val DEFAULT_RETRY_TIMEOUT = (5 * 1000).toLong() // 5 seconds
        private val log = LoggerFactory.getLogger(RedisSessionStoreImpl::class.java)
    }
}
