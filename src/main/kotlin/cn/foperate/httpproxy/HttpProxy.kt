package cn.foperate.httpproxy

import io.vertx.core.Handler
import io.vertx.core.http.HttpClient
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext

class HttpProxy(val client:HttpClient, val port:Int, val host:String): Handler<RoutingContext> {

    override fun handle(event: RoutingContext) {
        val req = event.request()
        log.debug("Proxying request: " + req.uri())
        val cReq = client.request(req.method(), port, host, req.uri()) { cRes ->
            log.debug("Proxying response: " + cRes.statusCode())

            val resp = event.response()
            resp.setStatusCode(cRes.statusCode())
            resp.headers().addAll(cRes.headers())
            resp.setChunked(true)
            cRes.pipeTo(resp)
            cRes.endHandler{
                resp.end()
            }
        }
        cReq.headers().addAll(req.headers())
        cReq.setChunked(true)
        req.pipeTo(cReq)
        req.endHandler{
            log.debug("end of the request")
            cReq.end()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(HttpProxy::class.java)
    }
}
