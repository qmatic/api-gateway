FROM openjdk:8-slim-buster

ENV APP_HOME /opt/qmatic/api_gw 
RUN groupadd -r qmatic && \
        useradd --no-log-init -rmd /home/qmatic -g qmatic qmatic && usermod -aG sudo qmatic && \
        echo "qmatic:qmatic" | chpasswd

RUN mkdir -p /opt/qmatic/api-gw
COPY QP_APIGateway_linux64-1.4.6.4.zip /opt/qmatic/api-gw/

RUN cd /opt/qmatic/api-gw/; jar -xvf QP_APIGateway_linux64-1.4.6.4.zip

COPY do_start.sh /opt/qmatic/api-gw/bin/
COPY conf/application.yml /opt/qmatic/api-gw/conf/
COPY conf/logbackAPIGateway.xml /opt/qmatic/api-gw/conf/

## RUN /opt/qmatic/api-gw/bin/api-token-generator >> /opt/qmatic/api-gw/conf/token
RUN touch /opt/qmatic/api-gw/logs/api_gateway.log
RUN chown -R qmatic:qmatic /opt/qmatic/api-gw
RUN chmod a+x /opt/qmatic/api-gw/bin/*

RUN su - qmatic -c "sed -i 's/gwUser=\"@GATEWAY-USER@\"/gwUser=qmatic/g' /opt/qmatic/api-gw/bin/api-gateway-service" ;\
    su - qmatic -c "sed -i 's/gwHome=\"@GATEWAY-INSTALL-DIR@\"/gwHome=\/opt\/qmatic\/api-gw\//g' /opt/qmatic/api-gw/bin/api-gateway-service";
   
EXPOSE 9091
EXPOSE 9090

CMD ["/opt/qmatic/api-gw/bin/do_start.sh"]