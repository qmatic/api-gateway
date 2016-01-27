# api-gateway

 ## Description
 The API Gateway is an open source middle-ware enabling end users, apps and webpages to connect to Orchestra in a secure and controlled way, limiting the load on Orchestra, even for large number of users.

 It can also be seen as the front door for all requests from devices and web sites to the Orchestra central server. Removing the need to expose the Central Orchestra server to Internet.

 ## Features

 [x] Request Caching
 Decreases the load on Orchestra Central.

 [x] API Authorisation Token
 Provides identifictaion, separation of applications and security.

 [x] Geo caching
 Locates closest services and branches from any position.

 ## Building
 The API Gateway project is built using gradle (currently version 2.10), for example: 'gradle clean build'

 ## Using
 The API Gateway can be either be installed from the the './build/distributions/qp-api-gateway.zip, or started by running 'gradle bootRun'

 ##Example
 The new API MobileTicket, which is optimized for the mobile ticket scenario as listed below:

```
 * get branches for service - /GEO/services/[serviceId]/branches
 * get nearest branches - /GEO/services/[serviceId]/nearestbranches
 * issue ticket - /MobileTicket/services/[serviceId]/branches/[branchId]/ticket/issue
 * get position in queue - /MobileTicket/MyVisit/Position/branches/[branchId]/queues/[queueId]/visits?visitId=[visitId]
 * get last event for visit - /MobileTicket/MyVisit/LastEvent/branches/[branchId]/visits/[visitId]/events?visitId=[visitId]
```