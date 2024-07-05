#!/bin/bash

# Define ports for the services
PORT1=8081
PORT2=8082
PORT3=8083

# Define REMOTE_URL for the services
REMOTE_URL1="http://localhost:${PORT2}"
REMOTE_URL2="http://localhost:${PORT3}"

export SERVER_URL=https://8a249624e4f345e08e5f3dbf60c19fd3.apm.us-west2.gcp.elastic-cloud.com:443

# Start service1
java \
-javaagent:opentelemetry-javaagent.jar \
-Dotel.exporter.otlp.endpoint=${SERVER_URL} \
-Dotel.exporter.otlp.headers="Authorization=Bearer ${SECRET_KEY}" \
-Dotel.metrics.exporter=otlp \
-Dotel.logs.exporter=otlp \
-Dotel.resource.attributes=service.name=favorite-service1,service.version=0.0.1,deployment.environment=production \
-Dotel.service.name=favorite-service1 \
-Dotel.javaagent.extensions=opentelemetry-custom-instrumentation/target/opentelemetry-custom-instrumentation-1.0-SNAPSHOT.jar \
-Dotel.javaagent.debug=true \
-Dserver.port=${PORT1} \
-DREMOTE_URL=${REMOTE_URL1} \
-Dyaml.file.name=/Users/davidhope/IdeaProjects/custom-instrumentation-examples/instrumentation-config.yml \
-Dotel.traces.sampler=always_on \
-jar java-favorite-otel-auto-service1/target/favorite-0.0.1-SNAPSHOT.jar 2>&1 >> log1.log &

# Start service2
java \
-javaagent:opentelemetry-javaagent.jar \
-Dotel.exporter.otlp.endpoint=${SERVER_URL} \
-Dotel.exporter.otlp.headers="Authorization=Bearer ${SECRET_KEY}" \
-Dotel.metrics.exporter=otlp \
-Dotel.logs.exporter=otlp \
-Dotel.resource.attributes=service.name=favorite-service2,service.version=0.0.1,deployment.environment=production \
-Dotel.service.name=favorite-service2 \
-Dotel.javaagent.extensions=opentelemetry-custom-instrumentation/target/opentelemetry-custom-instrumentation-1.0-SNAPSHOT.jar \
-Dotel.javaagent.debug=true \
-Dserver.port=${PORT2} \
-DREMOTE_URL=${REMOTE_URL2} \
-Dyaml.file.name=/Users/davidhope/IdeaProjects/custom-instrumentation-examples/instrumentation-config.yml \
-Dotel.traces.sampler=always_on \
-jar java-favorite-otel-auto-service2/target/favorite-0.0.1-SNAPSHOT.jar 2>&1 >> log2.log &

# Start service3
java \
-javaagent:opentelemetry-javaagent.jar \
-Dotel.exporter.otlp.endpoint=${SERVER_URL} \
-Dotel.exporter.otlp.headers="Authorization=Bearer ${SECRET_KEY}" \
-Dotel.metrics.exporter=otlp \
-Dotel.logs.exporter=otlp \
-Dotel.resource.attributes=service.name=favorite-service3,service.version=0.0.1,deployment.environment=production \
-Dotel.service.name=favorite-service3 \
-Dotel.javaagent.extensions=opentelemetry-custom-instrumentation/target/opentelemetry-custom-instrumentation-1.0-SNAPSHOT.jar \
-Dotel.javaagent.debug=true \
-Dyaml.file.name=/Users/davidhope/IdeaProjects/custom-instrumentation-examples/instrumentation-config.yml \
-Dotel.traces.sampler=always_on \
-Dserver.port=${PORT3} \
-jar java-favorite-otel-auto-service3/target/favorite-0.0.1-SNAPSHOT.jar 2>&1 >> log3.log &
