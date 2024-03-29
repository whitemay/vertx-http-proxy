FROM adoptopenjdk:8-jre-openj9

RUN mkdir /verticles

#ENV VERTICLE_NAME cn.foperate.httpproxy.MainVerticle
ENV JAVA_OPTS "-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager"
ENV VERTX_OPTS "-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4j2LogDelegateFactory"
WORKDIR /verticles
EXPOSE 8080

ENTRYPOINT ["sh", "-c"]
# 卡了好半天，entrypoint要求参数必须为一个字符串，而cp里面使用通配符必须带引号
#CMD ["java -cp \"./*\" io.vertx.core.Launcher run $VERTICLE_NAME"]
CMD ["java $JAVA_OPTS -jar proxy-all.jar $VERTX_OPTS"]

COPY build/libs/proxy-all.jar /verticles/proxy-all.jar
