DCAE SDK
===============
Because most services and collectors deployed on DCAE platform relies on similar microservices a common Software Development Kit has been created. It contains utilities and clients which may be used when getting configuration from CBS, consuming messages from DMaaP, interacting with A&AI, etc. SDK is written in Java.

## Reactive programming
Most of SDK APIs are using Project Reactor, which is one of available implementations of Reactive Streams (as well as Java 9 Flow). This way we support both high-performance, non-blocking asynchronous clients and old-school, thread-bound, blocking clients. We believe that using reactive programming can solve many cloud-specific problems for us - if used properly.

## Artifacts

#### Current version
````
<properties>
  <sdk.version>1.1.4</sdk.version>
</properties>`
````
#### Maven dependencies
````
<dependencies>
  <dependency>
    <groupId>org.onap.dcaegen2.services.sdk.rest.services</groupId>
    <artifactId>cbs-client</artifactId>
    <version>${sdk.version}</version>
  </dependency>
 
  <dependency>
    <groupId>org.onap.dcaegen2.services.sdk.security.crypt</groupId>
    <artifactId>crypt-password</artifactId>
    <version>${sdk.version}</version>
  </dependency>
 
  <dependency>
    <groupId>org.onap.dcaegen2.services.sdk</groupId>
    <artifactId>hvvesclient-producer-api</artifactId>
    <version>${sdk.version}</version>
  </dependency>
  <dependency>
    <groupId>org.onap.dcaegen2.services.sdk</groupId>
    <artifactId>hvvesclient-producer-impl</artifactId>
    <version>${sdk.version}</version>
    <scope>runtime</scope>
  </dependency>
 
 <!-- more to go -->
 
</dependencies>
````

## Changelog 
Below link is a reference to an internet site which contains information about the changelog.
[DCAE_SDK_Changelog](https://wiki.onap.org/display/DW/DCAE+SDK+Changelog)


## License

Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
[License](http://www.apache.org/licenses/LICENSE-2.0)