package cn.foperate.httpproxy

import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.http.httpClientOptionsOf
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle

/****
 * It is a simple HTTP reverse proxy, served for only one server.
 * 2 ways to start it: as an application or as a verticle.
 */
class MainVerticle(var port:Int): CoroutineVerticle() {

    private val backHost = System.getenv("BACK_SERVER") ?: "127.0.0.1"
    private val backPort = System.getenv("BACK_PORT") ?: "80"

    // Launcher过程要重入两次才生效。目前看来在这里设置日志是最佳的选择。
    init {
        // Start log to log4j2, but launch still using log4j.
        System.setProperty("vertx.logger-delegate-factory-class-name", Log4j2LogDelegateFactory::class.java.name)
        LoggerFactory.initialise()

        if (backPort!="80") {
            log.error("设置了服务端口")
        }
    }

    // 这里是为了与用Main启动兼容
    constructor():this(8080) {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
    }

    override suspend fun start() {
        val options = httpClientOptionsOf(
                maxInitialLineLength = 10000,
                logActivity = true,
                keepAlive = true
        )
        val client = vertx.createHttpClient(options)

        val router = Router.router(vertx)
        router.route().handler(HttpFilter(coroutineContext))
        router.route().handler(HttpProxy(client, backPort.toInt(), backHost))
        val proxyServer = vertx.createHttpServer(HttpServerOptions()
                .setPort(port)
                .setMaxInitialLineLength(10000)
                .setLogActivity(true))
                .requestHandler(router)
        proxyServer.listenAwait()
        log.debug("Proxy server started on $port")
   }

    companion object {
        private val log: Logger by lazy {
            LoggerFactory.getLogger(MainVerticle::class.java)
        }
    }
}