#!/bin/bash

/opt/qmatic/api-gw/bin/api-gateway

while true
do 
        tail -f  /opt/qmatic/api-gw/logs/api_gateway.log
done
