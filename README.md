#api-gateway

##Description
The API Gateway is an open source middle-ware enabling end users, apps and webpages to connect to Orchestra in a secure and controlled way, limiting the load on Orchestra, even for large number of users.

It can also be seen as the front door for all requests from devices and web sites to the Orchestra central server. Removing the need to expose the Central Orchestra server to Internet.

###Features

#####Request Caching
Decreases the load on Orchestra Central.

#####API Authorisation Token
Provides identifictaion, separation of applications and security.

#####Geo Cache
Locates closest services and branches from any position.

###Building
The API Gateway project is built as a Spring Boot Application (https://spring.io/guides/gs/spring-boot), using Netflix zuul edge service framework (https://github.com/Netflix/zuul).
 
* Clone this repo `git clone https://www.github.com/qmatic/api-gateway.git`
* Build project using gradle or gradle wrapper, for example: `gradlew clean build`

###Using
 The API Gateway can be either be installed from the the `./build/distributions/qp-api-gateway.zip` or started by running `gradlew bootRun`

###Example
 The new API MobileTicket, which is optimized for the mobile ticket scenario as listed below:

```
* get branches for service - /GEO/services/[serviceId]/branches
* get nearest branches - /GEO/services/[serviceId]/nearestbranches
* issue ticket - /MobileTicket/services/[serviceId]/branches/[branchId]/ticket/issue
* get position in queue - /MobileTicket/MyVisit/Position/branches/[branchId]/queues/[queueId]/visits?visitId=[visitId]
* get last event for visit - /MobileTicket/MyVisit/LastEvent/branches/[branchId]/visits/[visitId]/events?visitId=[visitId]
```
###Curl examples

```
* Get all branches using a token for authentication
curl -i -X GET -H "auth-token:846f91fe-b733-4597-97c3-f4fdd9740a14" http://localhost:9090/rest/servicepoint/branches
```