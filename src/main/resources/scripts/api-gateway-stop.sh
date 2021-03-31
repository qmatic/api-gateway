#!/usr/bin/env bash

fn_waitForAPIGatewayToStop(){
    echo "Waiting for APIGateway to stop"
    noOfStopLoops=1
    while  ps -p $PID > /dev/null 
    do
        sleep 10
        # Loop max 5 minutes
        ((noOfStopLoops++)) && (($noOfStopLoops==30)) && echo "Failed to stop in 5 minutes." && return 1
        echo "Waiting ten more seconds for APIGateway to stop"
    done
    return 0
}

# Find QP_HOME
GW_HOME="$(cd "$(dirname "$0")/.." && pwd -P)"

PORT=$(awk '/management:/,/port:/' $GW_HOME/conf/application.yml | awk '/port:/' | awk '{ print $2 }')

if [ -f $GW_HOME/.pid ]; then
  PID=$( cat $GW_HOME/.pid )
else
  PID=$(ps -ef | grep 'qp-api-gateway' | grep -v grep | awk '{print $2}' )
fi

$GW_HOME/bin/curl.ermine -X POST http://localhost:$PORT/api-gateway/shutdown
fn_waitForAPIGatewayToStop

if [ $? = 1 ]; then
    #If the shutdown fails, kill the api-gateway process.

    if [ $? = 0 ]; then
        echo "Doing forced shutdown of $PID"
        kill -9 $PID
        echo "Forced shutdown of $PID complete"
    fi
fi

rm $GW_HOME/.pid