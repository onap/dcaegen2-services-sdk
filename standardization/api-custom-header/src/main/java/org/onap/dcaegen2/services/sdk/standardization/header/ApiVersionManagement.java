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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.dcaegen2.services.sdk.standardization.util.ObjectManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

public final class ApiVersionManagement {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiVersionManagement.class);
  private static final Object INSTANCE = new Object();
  private static final String API_VERSION = "apiVersion";

  private static Map<String, List<ApiVersionModel>> verModelMap;

  /**
   * private constructor
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
    if (null != verModelMap) {
      List<ApiVersionModel> apiLst = verModelMap.get(requestedApiName);
      return ObjectManagement.isCollectionEmpty(apiLst) ? Collections.emptyList()
          : Collections.unmodifiableList(apiLst);
    }

    synchronized (INSTANCE) {
      if (null == verModelMap) {
        verModelMap = new HashMap<>();
        String apiVerJsonStr = readApiVersions(apiFilePath);

        // component not pass the apiversion parameter
        if (apiVerJsonStr.isEmpty()) {
          return Collections.emptyList();
        }

        // read service or resource api and put in HashMap one by one.
        convertVerToModel(parseApiVersion(apiVerJsonStr));
      }
    }

    List<ApiVersionModel> apiLst = verModelMap.get(requestedApiName);
    return ObjectManagement.isCollectionEmpty(apiLst) ? Collections.emptyList()
        : Collections.unmodifiableList(verModelMap.get(requestedApiName));
  }

  /**
   * parse api versions from api json string
   * 
   * @param apiVerJsonStr all api versions in json format
   * @return api versions<apikey, List<version>> in map format
   */
  private static Map<String, Object> parseApiVersion(String apiVerJsonStr) {
    Map<String, Object> apiMap = new Gson().fromJson(apiVerJsonStr, Map.class);
    if (apiMap.containsKey(API_VERSION)) {
      return apiMap;
    }

    // there is no apiVersion filed in apiVerJsonStr or the format is wrong.
    LOGGER.error("the struct of api version is wrong, so return the empty api map.");
    return Collections.emptyMap();
  }

  /**
   * convert api version into ApiVersionModel object
   * 
   * @param apiMap Map object for all api versions
   */
  private static void convertVerToModel(Map<String, Object> apiMap) {
    if (apiMap.isEmpty()) {
      verModelMap = Collections.emptyMap();
      return;
    }

    List<Object> currApiArray = null;
    Map<String, Object> apiVersMap = (apiMap.get(API_VERSION) instanceof Map) ? (Map) apiMap.get(API_VERSION) : null;
    if (null != apiVersMap) {
      for (Map.Entry<String, Object> entry : apiVersMap.entrySet()) {
        currApiArray = (entry.getValue() instanceof List) ? (List) entry.getValue() : null;
        if (null != currApiArray) {
          List<ApiVersionModel> currApiLst = new ArrayList<>();
          currApiArray.forEach(currApi -> currApiLst.add(new ApiVersionModel(currApi.toString())));
          verModelMap.put(entry.getKey(), currApiLst);
        }
      }
    }
  }

  /**
   * read api version from path
   * 
   * @param filePath api versions
   * @return api versions json string
   */
  private static String readApiVersions(String filePath) {
    try {
      return new String(Files.readAllBytes(Paths.get(filePath)));
    } catch (IOException e) {
      LOGGER.error("Fail to read api version file", e);
    }

    return "";
  }
}