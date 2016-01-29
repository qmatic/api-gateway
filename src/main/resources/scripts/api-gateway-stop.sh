#!/usr/bin/env bash

fn_waitForAPIGatewayToStop(){
    echo "Waiting for APIGateway to stop"
    noOfStopLoops=1
    while  ps ax | grep java | grep qp-api-gw | grep -v grep > /dev/null
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

$GW_HOME/bin/curl.ermine -X POST http://localhost:9091/shutdown
fn_waitForAPIGatewayToStop

# Standard install use the java from its install dir
if [ "Linux" == `uname` ]
then
    export JAVA_HOME=$GW_HOME/jre1.8.0_72
    export PATH=$JAVA_HOME/bin:$PATH

    JPS=$JAVA_HOME/bin/jps
else
    JPS=`which jps`
fi

if [ $? = 1 ]; then
    #If the shutdown fails, kill the jboss process.
    ps=`$JPS -vl | grep 'qp-api-gw'`
    if [ $? = 0 ]; then
        echo "Doing forced shutdown of $ps"
        echo $ps | awk '{print $1}' | xargs kill -9
        echo "Forced shutdown of $ps complete"
    fi
fi

