# Pubsub REST proxy

Use the REST way to push JSON notification to the system.

## Usage

### Push notification to the platform

> curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d ' {"user":{"first_name":"firstname","last_name":"lastname","email":"email@email.com","password":"app123","password_confirmation":"app123"}}'  http://localhost:8080/play-rest-pubsubproxy/rest/pubsub/notify/TwitterFeed/

This will get the topic information from the governance (for TwitterFeed in this example), create a WSN notification with JSON as body and send the notification to the DSB.

### Get current topics

> curl http://localhost:8080/play-rest-pubsubproxy/rest/pubsub/topics
