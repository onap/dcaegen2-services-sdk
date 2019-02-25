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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiVersionManagement {

  private final static Logger LOGGER = LoggerFactory.getLogger(ApiVersionManagement.class);
  private final static Object INSTANCE = new Object();

  private static Map<String, List<ApiVersionModel>> verMap;

  /**
   * constructor
   */
  private ApiVersionManagement() {

  }

  /**
   * get api version from json string
   * 
   * @param apiFilePath api file path
   * @param requestedApiName current api name
   * @return all api version for a major version
   */
  public static List<ApiVersionModel> getApiVersion(String apiFilePath, String requestedApiName) {
    if (null != verMap) {
      List<ApiVersionModel> apiLst = verMap.get(requestedApiName);
      return CollectionUtils.isEmpty(apiLst) ? null : Collections.unmodifiableList(apiLst);
    }

    synchronized (INSTANCE) {

      verMap = new HashMap<String, List<ApiVersionModel>>();
      String apiVerJsonStr = readApiVersions(apiFilePath);

      // component not pass the apiversion parameter
      if (StringUtils.isEmpty(apiVerJsonStr)) {
        return null;
      }

      // there is no apiVersion filed in apiVerJsonStr or the format is wrong.
      JSONObject jsonObjectVer = new JSONObject(apiVerJsonStr);
      if (!(jsonObjectVer.has("apiVersion") && jsonObjectVer.get("apiVersion") instanceof JSONObject)) {
        return null;
      }

      // read service or resource api and put in HashMap one by one.
      JSONArray currApiArray = null;
      JSONObject jsonObj = (JSONObject) jsonObjectVer.get("apiVersion");
      for (String currApiame : jsonObj.keySet()) {
        currApiArray = (jsonObj.get(currApiame) instanceof JSONArray) ? (JSONArray) jsonObj.get(currApiame) : null;
        if (null != currApiArray) {
          List<ApiVersionModel> currApiLst = new ArrayList<ApiVersionModel>();
          currApiArray.forEach(currApi -> {
            currApiLst.add(new ApiVersionModel(currApi.toString()));
          });
          verMap.put(currApiame, currApiLst);
        }
      }
    }

    List<ApiVersionModel> apiLst = verMap.get(requestedApiName);
    return CollectionUtils.isEmpty(apiLst) ? null : Collections.unmodifiableList(verMap.get(requestedApiName));
  }

  /**
   * read api version from path
   * 
   * @param filePath api version file
   * @return api versions
   */
  private static String readApiVersions(String filePath) {
    try {
      return FileUtils.readFileToString(new File(filePath));
    } catch (IOException e) {
      LOGGER.error("Fail to read api version from {}", filePath);
    }

    return null;
  }
}
