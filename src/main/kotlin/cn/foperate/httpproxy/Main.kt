package cn.httpproxy

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import io.vertx.core.Vertx
import cn.foperate.httpproxy.MainVerticle
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.Log4J2LoggerFactory
import io.vertx.core.logging.LoggerFactory

object Main {
    private val log = LoggerFactory.getLogger(Main::class.java)

    @Parameter(names = ["--port"])
    var port = 8080

    @Parameter(names = ["--address"])
    var address = "0.0.0.0"

    @JvmStatic
    fun main(args:Array<String>) {

        log.info("I am here")
        val jc = JCommander(Main)
        jc.parse(*args)
        log.info(Main.toString())

        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE)
        Vertx.vertx().deployVerticle(MainVerticle(port))
    }
}