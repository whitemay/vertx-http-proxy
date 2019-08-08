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
        /*****
         * 处理逻辑：
         * 1、首先判断请求中是否包含token cookie，
         * 2、如果有token cookie，则去检查Redis中是否有对应的permission记录，
         * 3、没有的话，返回401，同时要求客户端删除token;
         * 4、有permission，则根据该permission检查用户的请求是否合法，并完成后续的动作；
         * 5、没有cookie，则检查是否有token头，然后同样检查是否包括对应的permission记录；
         * 6、包括的话，将token写入用户cookie，并直接返回/要求用户重新请求。
         */
        log.debug("目前全部放过")
        event.next()
    }

    companion object {
        private val log = LoggerFactory.getLogger(HttpFilter::class.java)
    }
}