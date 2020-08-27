Stuff to run a simple load test.
================================

docker-apigw/
    To create a docker image to run api-gw you need to copy the api-gw zip-file into this folder.
    Update the Dockerfile with correct name for QP_APIGateway_linux64-<version>>.zip, there is 2 places.
    build the image with
    >docker build -t api_gw .

Run api-gw with docker-compose from this folder
>docker-compose up

Mock server and load-test is done from Soap-UI. (It's fine with the open version) (Soap-UI needs java 11 to work fine on ubuntu)
The soap-iu project file is Mobile-check-visit-status-soapui-project.xml

This project include a mock-service simulating a orchestra with endpoints for 2 branches.
Branch 1 is fine and always respond. And Branch 2 is more flaky, 1/3 of responses is OK.
