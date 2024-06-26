# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.9.5] - 2024/05/16
#### Added
      - [DCAEGEN2-3411] - Validation of OpenAPI files in externalrepo fails

## [1.9.4] - 2023/02/23
#### Added
      - [DCAEGEN2-3364] - To use kafka API instead of DMAAP API in dmaap-client

## [1.9.3] - 2023/02/23
#### Added
      - [DCAEGEN2-3344] - Upgrade dependencies for dcaegen2-services-sdk


## [1.9.2] - 2023/02/17
#### Added
      - [DCAEGEN2-3352] - Enhance services-sdk's security module JUnits to have OS agnostic path

## [1.9.1] - 2022/09/07
### Added
    - [DCAEGEN2-3165] (https://jira.onap.org/browse/DCAEGEN2-3165) - Fix calculation of code coverage
    - [DCAEGEN2-3165] (https://jira.onap.org/browse/DCAEGEN2-3165) - increase code coverage 

## [1.9.0] - 2022/09/07
### Added
    - [DCAEGEN2-3098] (https://jira.onap.org/browse/DCAEGEN2-3098) - Remove Consul and CBS property dependencies from CBS-client SDK

## [1.8.10] - 2022/07/29
### Added
    - [DCAEGEN2-3220] (https://jira.onap.org/browse/DCAEGEN2-3220) - Fix SDK Vulnerability. Top up Spring-Boot version to 2.7.2

## [1.8.9] - 2022/07/15
### Added
    - [DCAEGEN2-3223] (https://jira.onap.org/browse/DCAEGEN2-3223) - Fix CBS client environment variable substitution fails for complex cases

## [1.8.8] - 2022/02/07
### Added
    - [DCAEGEN2-3051] (https://jira.onap.org/browse/DCAEGEN2-3051) - Fix SDK Vulnerability. Top up Spring-Boot version to 2.5.9

## [1.8.7] - 2021/08/02
### Added
    - [DCAEGEN2-2692] (https://jira.onap.org/browse/DCAEGEN2-2692) - Make CBS-Client config and policy file paths configurable by environment variables

## [1.8.6] - 2021/06/07
### Added
    - [DCAEGEN2-2827] (https://jira.onap.org/browse/DCAEGEN2-2827) - Handle 429 error Too Many Requests

## [1.8.5] - 2021/06/02
### Added
    - [DCAEGEN2-2752] (https://jira.onap.org/browse/DCAEGEN2-2752) - Update CBS-Client to read policy configuration from a file exposed by policy-sidecar container

## [1.8.4] - 2021/05/14
### Added
    - [DCAEGEN2-2716] (https://jira.onap.org/browse/DCAEGEN2-2716) - Add to Java CBS-Client ability to resolve evns in app-config.yaml loaded from ConfigMap

## [1.8.3] - 2021/04/29
### Added
    - [DCAEGEN2-2716] (https://jira.onap.org/browse/DCAEGEN2-2716) - Adapt CBS-CLient to read configuration from a file exposed in a cfgMap

## [1.8.2] - 2021/03/30
### Added
    - [DCAEGEN2-2701] (https://jira.onap.org/browse/DCAEGEN2-2701) - Add stndDefinedNamespace field to CommonEventHeader

## [1.8.1] - 2021/03/25
### Fixed
    - [DCAEGEN2-2670] (https://jira.onap.org/browse/DCAEGEN2-2670) - Support authorized topics in DMaaP-Client
        - Remove test dependencies usage from runtime code

## [1.8.0] - 2021/03/10
### Added
    - [DCAEGEN2-2670] (https://jira.onap.org/browse/DCAEGEN2-2670) - Support authorized topics in DMaaP-Client

## [1.7.0] - 2021/02/25
### Added
    - [DCAEGEN2-1483] (https://jira.onap.org/browse/DCAEGEN2-1483) - VESCollector Event ordering
        - Add possibility to modify the configuration for persistent connection
        - Support retry-after header in DCAE-SDK DMaaP-Client

## [1.6.0] - 2021/02/03
    - Add configurable timeout in dmaap-client
    - Add configurable retry mechanism in dmaap-client

## [1.5.0] - 2020/11/26
    - Update spring boot to version: 2.4.0
    - Update reactor to version: 2020.0.1
    - Update testcontainers to version: 1.15.0
 
## [1.4.4] - 2020/11/19
    - Fix CbsClientFactory to allow retry on Mono from createCbsClient

## [1.4.3] - 2020/08/31
    - Change parameters of external-schema-manager to JSON notation

## [1.4.2] - 2020/08/18
    - Update spring boot to version: 2.3.3.RELEASE

## [1.4.1] - 2020/07/27
    - Update spring boot to version: 2.2.9.RELEASE
    - Update testcontainers version:  1.14.3
    - Fix deprecation warnings

## [1.4.0] - 2020/07/27
    - Add new component external-schema-manager for json validation with schema stored in local cache

## [1.3.6] - 2020/03/13
    - snapshot version changed to 1.3.6

## [1.3.5] - 2020/03/09
    - Create jar without dependencies for crypt-password module

## [1.3.4] - 2019/12/10
    - Usage of Java 11

## [1.3.3] - 2019/11/13
    - Upgrade CBS to support SSL
    - Fix static code vulnerabilities
    - Exclude IT from tests
    - Remove AAI client from SDK

## [1.3.2] - 2019/10/02
    - Restructure AAI client
    - Get rid of common-dependency module
    - Rearrange files in packages inside rest-services

## [1.3.1] - 2019/09/26
    - Bugfix release: AAI client
        - Make AaiGetServiceInstanceClient build correct path to the service resource in AAI

## [1.3.0] - 2019/06/14
    - (ElAlto - under development) ##
    - All El-Alto work noted under 1.2.0-SNAPSHOT will roll into this version
    - Version update was done for tracking global-jjb migration work and corresponding submission - https://gerrit.onap.org/r/#/c/dcaegen2/services/sdk/+/89902/

## [1.2.0] - 2019/05/27
    - (replaced by 1.3.0) ##
    - WARNING: This is a work in progress. Do not use unless you know what you are doing!

    - DMaaP client
        - Change the factory so it's more configurable
        - Old DMaaP client is now deprecated
        - Integration tests are now using TestContainers with an actual DMaaP in order to confirm compatibility with a particular DMaaP version.
        - Breaking change: MessageRouterSubscribeResponse now contains list of JsonElement instead of JsonArray
    - CBS client
        - Use new, simplified CBS lookup method
        - Breaking change: CbsClientConfiguration replaces old EnvProperties. This way the class reflects overall SDK naming convention.
    - Crypt Password
        - Additional command line usage options (read password from stdin)
        - Enhanced test coverage
    - Internals/others
        - Remove CloudHttpClient and use RxHttpClient instead which should unify REST API consumption across client libraries
    - Moher (MOnitoring and HEalthcheck Rest API)
        - This API is in incubation stage. Do not use it yet.
        - Initial PoC for new module which should help when implementing these features in a DCAE service
        - Expose Prometheus-compliant monitoring endpoint

## [1.1.6] - 2019/05/07
    - Bugfix release: (Old) DMaaP client:
        - Security keys was always loaded from JAR instead of given file system path. Only code using SecurityKeysUtil class had been affected. If you do not use SecurityKeysUtil class or you are using the new DMaaP MR client API (org.onap.dcaegen2.services.sdk.rest.services.dmaap.client.{api, model} packages) then you are safe and the update is not required.

## [1.1.5] 2019/05/07
    - DMaaP client
        - Force non-chunked transfer encoding, because DMaaP MR does not support chunks.
        - DMaaP MR client API should be used in new code. Some minor incompatible changes can occur but it's more or less done.

## [1.1.4] - 2019/03/06
    - Config Binding Service client
        - predefined parsers for input and output streams
            - remove the need for a DCAE application to manually interpret streams_publishes (Sinks) and streams_subscribes (Sources) parts of the configuration
            - available parsers for DMaaP Message Router and DMaaP Data Router streams
            - experimental support for Kafka streams
        - support for other CBS endpoints besides get-configuration: get-by-key, get-all (introduces minor but breaking changes)
    - DMaaP client
        - New, experimental DMaaP client. It's not ready for use yet (not integration tested with DMaaP instance). However, you can use this API if you target El Alto release (note that some minor interface changes might be introduced).
    - Internals:
        - Improved http client: RxHttpClient
        - RxHttpClient uses chunked transfer-encoding only when content-length is NOT specified.

Migration guide

All CbsClient methods gets CbsRequest as a first parameter instead of RequestDiagnosticContext. The CbsRequest may be created by calling CbsRequests factory methods. For existing code to work you will need to do the following change:

.. code-block:: java

    // From this:
    CbsClientFactory.createCbsClient(env)
        .flatMap(cbsClient -> cbsClient.get(diagnosticContext))
        ...

    // To this:
    final CbsRequest request = CbsRequests.getConfiguration(diagnosticContext);
    CbsClientFactory.createCbsClient(env)
        .flatMap(cbsClient -> cbsClient.get(request))
        ...

The similar changes will be required for other CbsClient methods (periodic get and periodic updates).

## [1.1.3] - 2019/03/01
    - initial release
    - Config Binding Service client
        - basic functionality
        - CBS service discovery
        - get application configuration as JsonObject
        - periodic query + periodic updates query
    - BCrypt password utility
