package cn.foperate.httpproxy

import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServerOptions
import io.vertx.httpproxy.HttpProxy
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.core.logging.Log4j2LogDelegateFactory
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory


/****
 * It is a simple HTTP reverse proxy, served for only one server.
 * 2 ways to start it: as an application or as a verticle.
 */
class MainVerticle(val port:Int): CoroutineVerticle() {

    init {
        // Start log to log4j2, but launch still using log4j.
        System.setProperty("vertx.logger-delegate-factory-class-name", Log4j2LogDelegateFactory::class.java.name)
        LoggerFactory.initialise()
    }

    constructor():this(8080) {
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
    }

    override suspend fun start() {
        val client = vertx.createHttpClient(HttpClientOptions()
                .setMaxInitialLineLength(10000)
                .setLogActivity(true))
        val proxy = HttpProxy.reverseProxy(client)
                .target(8081, "96.126.115.136")
        val proxyServer = vertx.createHttpServer(HttpServerOptions()
                .setPort(port)
                .setMaxInitialLineLength(10000)
                .setLogActivity(true))
                .requestHandler { req ->
                    log.debug("------------------------------------------")
                    log.debug(req.path())
                    proxy.handle(req)
                }
        proxyServer.listenAwait()
        log.debug("Proxy server started on $port")
    }

    companion object {
        private val log: Logger by lazy {
            LoggerFactory.getLogger(MainVerticle::class.java)
        }
    }
}