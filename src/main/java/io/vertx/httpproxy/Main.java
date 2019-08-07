package io.vertx.httpproxy;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  @Parameter(names = "--port")
  public int port = 8080;

  @Parameter(names = "--address")
  public String address = "0.0.0.0";

  public static void main(String[] args) {
    Main main = new Main();
    JCommander jc = new JCommander(main);
    jc.parse(args);
    main.run();
  }

  public void run() {
    InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    log.info("Here");
    Vertx vertx = Vertx.vertx();
    HttpClient client = vertx.createHttpClient(new HttpClientOptions()
        .setMaxInitialLineLength(10000)
        .setLogActivity(true));
    HttpProxy proxy = HttpProxy
        .reverseProxy(client)
        .target(8081, "96.126.115.136");
    HttpServer proxyServer = vertx.createHttpServer(new HttpServerOptions()
        .setPort(port)
        .setMaxInitialLineLength(10000)
        .setLogActivity(true))
        .requestHandler(req -> {
          log.debug("------------------------------------------");
          log.debug(req.path());
          proxy.handle(req);
        });
    proxyServer.listen(ar -> {
      if (ar.succeeded()) {
        log.debug("Proxy server started on " + port);
      } else {
        ar.cause().printStackTrace();
      }
    });
  }
}
