package cn.foperate.base.session

import io.vertx.ext.web.sstore.SessionStore
import kotlin.coroutines.CoroutineContext

interface RedisSessionStore: SessionStore {
    fun getSessionStore(context:CoroutineContext) = RedisSessionStoreImpl(context)
}
