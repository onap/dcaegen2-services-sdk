/*-
 * ============LICENSE_START=======================================================
 * org.onap.dcaegen2.collectors.ves
 * ================================================================================
 * Copyright (C) 2019 vmware. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.dcaegen2.services.sdk.standardization.header;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomHeaderUtils {

  private final static Logger logger = LoggerFactory.getLogger(CustomHeaderUtils.class);

  /**
   * more detail:
   * https://wiki.onap.org/display/DW/ONAP+API+Common+Versioning+Strategy+%28CVS%29+Guidelines
   */
  private final static String API_MINIOR_VERSION = "X-MinorVersion";
  private final static String API_PATCH_VERSION = "X-PatchVersion";
  private final static String API_LATEST_VERSION = "X-LatestVersion";

  /**
   * identify whether validate minor version successfully
   */
  private final static ThreadLocal<Boolean> CLIENT_MINOR_VERSION_OK = new ThreadLocal<Boolean>();
  private String majorVersion = null;
  private String minorVersion = null;
  private List<ApiVersionModel> verLst = null;

  /**
   * 
   * @param requestMajorVer client request major version
   * @param reqHeaderMap request headers
   * @param apiFilePath api file path
   * @param requestedApiName api name that is requested by client
   * 
   */
  public CustomHeaderUtils(String requestMajorVer, Map<String, String> reqHeaderMap, String apiFilePath, String requestedApiName) {
    // all api versions
    if (null != requestedApiName && !requestedApiName.isEmpty()) {
      verLst = ApiVersionManagement.getApiVersion(apiFilePath, requestedApiName);
    }

    // major version that client requests
    this.majorVersion = requestMajorVer;

    // minor version that client requests
    this.minorVersion = (null == reqHeaderMap || reqHeaderMap.isEmpty()) ? null : reqHeaderMap.get(API_MINIOR_VERSION);

    CLIENT_MINOR_VERSION_OK.set(Boolean.TRUE);
  }

  /**
   * get the custom headers of response
   * 
   * @return rspHeader custom headers of response
   */
  public Map<String, String> getRspCustomHeader() {
    try {
      return calCustomHeader();
    } finally {
      CLIENT_MINOR_VERSION_OK.remove();
    }
  }

  /**
   * calculate the custom header for response
   * 
   * @return rspHeader custom headers of response
   */
  private Map<String, String> calCustomHeader() {
    if (null == this.verLst || this.verLst.isEmpty()) {
      logger.warn("there is no api version configured in server.");
      return new HashMap<String, String>();
    }

    // if client not send X-MinorVersion, return the first minor version that major version is requested
    if (null == this.minorVersion || this.minorVersion.isEmpty()) {
      return calHeadersNoMinorVer();
    }

    // if client send x-MinorVersion, minor vefrsion is wrong
    if (!CLIENT_MINOR_VERSION_OK.get()) {
      return calHeadersWithWrongMinorVer();
    }

    // client send X-MinorVersion and minor version is right
    return calHeadersWithMinorVer();
  }

  /**
   * calculate the custom header should be returned when client send minor version which is wrong
   * 
   * @return rspHeader custom headers of response
   */
  private Map<String, String> calHeadersWithWrongMinorVer() {
    Map<String, String> rspHeader = new HashMap<String, String>(3);

    // Latest version mean greatest version
    rspHeader.put(API_LATEST_VERSION, this.verLst.get(verLst.size() - 1).getVersion());

    int ipos = -1;
    ApiVersionModel currVer = null;
    for (int index = 0; index < this.verLst.size(); index++) {
      currVer = this.verLst.get(index);
      if (this.majorVersion.equals(currVer.getMajorVersion())) {
        ipos = index;
      }
    }

    if (-1 == ipos) {
      logger.warn("wrong apiVersion is provided, major {} not foud", this.majorVersion);
    } else {
      currVer = this.verLst.get(ipos);
      rspHeader.put(API_MINIOR_VERSION, currVer.getMinorVersion());
      rspHeader.put(API_PATCH_VERSION, currVer.getPatchVersion());
    }

    return rspHeader;
  }

  /**
   * calculate the custom header should be returned when client send minor version which is right
   * 
   * @return rspHeader custom headers of response
   */
  private Map<String, String> calHeadersWithMinorVer() {
    Map<String, String> rspHeader = new HashMap<String, String>(3);

    // Latest version mean greatest version
    rspHeader.put(API_LATEST_VERSION, this.verLst.get(verLst.size() - 1).getVersion());

    // set minor version
    rspHeader.put(API_MINIOR_VERSION, this.minorVersion);

    // set patch version
    ApiVersionModel currVer = null;
    for (int index = 0; index < this.verLst.size(); index++) {
      currVer = verLst.get(index);
      if (this.majorVersion.equals(currVer.getMajorVersion()) && this.minorVersion.equals(currVer.getMinorVersion())) {
        rspHeader.put(API_PATCH_VERSION, currVer.getPatchVersion());
        break;
      }
    }

    return rspHeader;
  }

  /**
   * calculate the custom header should be returned when client not send minor version
   * 
   * @return rspHeader custom headers of response
   */
  private Map<String, String> calHeadersNoMinorVer() {
    Map<String, String> rspHeader = new HashMap<String, String>(3);

    // Latest version mean greatest version
    rspHeader.put(API_LATEST_VERSION, this.verLst.get(verLst.size() - 1).getVersion());

    // the first version of major version
    ApiVersionModel currVer = null;
    for (int index = 0; index < this.verLst.size(); index++) {
      currVer = verLst.get(index);
      if (this.majorVersion.equals(currVer.getMajorVersion())) {
        rspHeader.put(API_MINIOR_VERSION, currVer.getMinorVersion());
        rspHeader.put(API_PATCH_VERSION, currVer.getPatchVersion());
        break;
      }
    }

    return rspHeader;
  }

  /**
   * Check header whether it is right.
   * 
   * @return true when validating successfully or minor version not exist
   */
  public Boolean isOkCustomHeaders() {
    if (null == this.minorVersion || this.minorVersion.isEmpty()) {
      logger.warn("X-MinorVersion is empty or null");
      return true;
    }

    ApiVersionModel currVer = null;

    // verList is an order array, which is from the first version to the latest version.
    for (int index = 0; index < this.verLst.size(); index++) {
      currVer = verLst.get(index);
      if (currVer.getMajorVersion().equals(this.majorVersion) && currVer.getMinorVersion().equals(this.minorVersion)) {
        return true;
      }
    }

    logger.error("not find major version {} and minor version {}", this.majorVersion, this.minorVersion);
    CLIENT_MINOR_VERSION_OK.set(Boolean.FALSE);
    return false;
  }
}
