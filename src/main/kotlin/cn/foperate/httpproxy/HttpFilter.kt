package cn.foperate.httpproxy

import io.vertx.core.Handler
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class HttpFilter(context: CoroutineContext): Handler<RoutingContext>,
        CoroutineScope by CoroutineScope(context) {
    override fun handle(event: RoutingContext) {
        launch { handler(event) }
    }


    private suspend fun handler(event: RoutingContext) {
        log.debug("目前全部放过")
        event.next()
    }

    companion object {
        private val log = LoggerFactory.getLogger(HttpFilter::class.java)
    }
}