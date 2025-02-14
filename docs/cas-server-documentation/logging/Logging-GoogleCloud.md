---
layout: default
title: CAS - Google Cloud Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Google Cloud Logging

[Cloud Logging](https://cloud.google.com/logging/) is the managed logging service provided by Google Cloud.

The integration here also provides automatic support for associating a web request trace ID with the corresponding log entries
by retrieving the `X-B3-TraceId` or `x-cloud-trace-context` header values.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gcp-logging" %}

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
Due to the way logging is set up, the Google Cloud project ID and credentials 
defined in CAS properties are ignored. Instead, you should set the <code>GOOGLE_CLOUD_PROJECT</code> 
and <code>GOOGLE_APPLICATION_CREDENTIALS</code> environment variables to the project ID and credentials 
private key location, where necessary. Alternatively, the Google Cloud project ID can also be set directly
in the logging configuration.</p></div>

This is an example of the logging configuration:

```xml
<Configuration packages="org.apereo.cas.logging">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <JsonLayout locationInfo="false"
                        includeStacktrace="true"
                        objectMessageAsJsonObject="true"
                        compact="true"
                        properties="false"
                        eventEol="true"
                        includeTimeMillis="false">
                <KeyValuePair key="time" value="$${event:timestamp:-}"/>
                <KeyValuePair key="timestampSeconds" value="$${ctx:timestampSeconds:-}"/>
                <KeyValuePair key="timestampNanos" value="$${ctx:timestampNanos:-}"/>
                <KeyValuePair key="severity" value="$${ctx:severity:-}"/>
                <KeyValuePair key="labels" value="$${ctx:labels:-}"/>
                <KeyValuePair key="httpRequest" value="$${ctx:httpRequest:-}"/>
                <KeyValuePair key="logging.googleapis.com/sourceLocation" value="$${ctx:sourceLocation:-}"/>
                <KeyValuePair key="logging.googleapis.com/spanId" value="$${ctx:spanId:-}"/>
                <KeyValuePair key="logging.googleapis.com/trace" value="$${ctx:traceId:-}"/>
            </JsonLayout>
        </Console>
        <!-- Update the projectId, or remove and let CAS determine the project id automatically -->
        <GoogleCloudAppender name="GoogleCloudAppender" 
                             projectId="...">
            <AppenderRef ref="Console"/>
        </GoogleCloudAppender>
    </Appenders>

    <Loggers>
        <Logger name="org.apereo.cas" includeLocation="true" 
                level="INFO" additivity="false">
            <AppenderRef ref="GoogleCloudAppender"/>
        </Logger>
    </Loggers>

</Configuration>
```
