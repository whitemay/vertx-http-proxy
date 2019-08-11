FROM adoptopenjdk:8-jre-openj9

RUN mkdir /verticles

COPY build/libs/proxy-0.0.2-all.jar /verticles/proxy-all.jar

#ENV VERTICLE_NAME cn.foperate.httpproxy.MainVerticle
ENV VERTX_OPTS "-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4j2LogDelegateFactory"
WORKDIR /verticles

ENTRYPOINT ["sh", "-c"]
# 卡了好半天，entrypoint要求参数必须为一个字符串，而cp里面使用通配符必须带引号
#CMD ["java -cp \"./*\" io.vertx.core.Launcher run $VERTICLE_NAME"]
CMD ["java -jar proxy-all.jar"]
