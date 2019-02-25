.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright 2019 vmware

1. Background
=============

Trouble shooting and tell client the latest api version under that major version is requested.

2. Propose
==========

https://wiki.onap.org/display/DW/ONAP+API+Common+Versioning+Strategy+%28CVS%29+Guidelines

3. HLD
======

3.1 Version Management
----------------------

Every component provides a json file or property configuration which is used to describe the api version detail. The example of json file content as below:

=================================================== ============= =====================================================================================================
{                                                                
                                                                 
"apiVersion":                                                    
                                                                 
{                                                                
                                                                 
"eventListener": ["4.7.2","5.3.2","5.4.1","7.0.1"],              
                                                                 
"xxxxxx": ["1.0.2","1.1.2","2.0.1"]                              
                                                                 
}                                                                
                                                                 
}                                                             
=================================================== ============= =====================================================================================================
Field                                               Value type    remark
apiVersion                                          Map           An identify that start to describe the api versions
eventListener                                       Array<String> A service, resource or function name of component, which is a unique identify of one api.
                                                                 
                                                                  Requirement: describe the api version **in sequence** that is from first version to greatest version.
=================================================== ============= =====================================================================================================

In the future, if there are other version configurations which need to be described, extend other fields.

3.2 Return response with custom headers
---------------------------------------

First, server should check the custom header of client, if X-MinorVersion does not exist which is maybe deletes after a period time or that client requests is wrong because of some reason, return response with errorcode 400 and the first major version including X-MinorVersion, X-PatchVersion, X-LatestVersion.

3.2.1 Minor version non-exist
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

For exmpale, ves has no 5.2.x version, so if the request of client is major version v5, X-MinorVersion is 2, the server return with 400 errorcode and custom headers including X-MinorVersion 4, X-PatchVersion 1, X-LatestVersion 5.4.1

=========================== ================== ================== ================== =================== ======================================================================================================================================
**Client send**             **Server return**                                             
=========================== ================== ================== ================== =================== ======================================================================================================================================
**major version requested** **X-MinorVersion** **X-MinorVersion** **X-PatchVersion** **X-LatestVersion** **remark**
v5                          2                  4                  1                  7.0.1               Fail to valid, return 400. X-MinorVersion and x-patchversion are under the last version that the major version is requested by client.
=========================== ================== ================== ================== =================== ======================================================================================================================================

3.2.2 Minor version exist
~~~~~~~~~~~~~~~~~~~~~~~~~

=========================== ================== ================== ================== =================== =====================================================
**Client send**             **Server return**                                              
=========================== ================== ================== ================== =================== =====================================================
**major version requested** **X-MinorVersion** **X-MinorVersion** **X-PatchVersion** **X-LatestVersion** **remark**
v5                          no                 3                  2                  7.0.1               Supported request; X-MinorVersion and x-patchversion are under the first version that the major 
v5                          4                  4                  1                  7.0.1               Valid request; respond with customer header
v5                          3                  3                  2                  7.0.1               Valid request; respond with customer header
v7                          no                 0                  1                  7.0.1               Supported request; notify client with customer header
v7                          0                  0                  1                  7.0.1               Supported request; notify client with customer header
=========================== ================== ================== ================== =================== =====================================================

3.3 Code Implement
------------------

CustomHeaderUtils is a class that provides functions to validate the X-MinorVersion which client requests and return the response header according to the client request.

The usage example:
------------------
CustomHeaderUtils util = new CustomHeaderUtils(requestMajorVer, reqHeaderMap, filePath, "eventListener");


// check request header

util.isOkCustomHeaders();


// get response header

Map<String, String> rspHeader = util.getRspCustomHeader()