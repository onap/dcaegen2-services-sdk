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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

public final class ApiVersionManagement {

  private final static Logger LOGGER = LoggerFactory.getLogger(ApiVersionManagement.class);
  private final static Object INSTANCE = new Object();

  private static Map<String, List<ApiVersionModel>> verMap;

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
      return (null == apiLst || apiLst.isEmpty()) ? null : Collections.unmodifiableList(apiLst);
    }

    synchronized (INSTANCE) {
      if(null == verMap)
      {
        verMap = new HashMap<String, List<ApiVersionModel>>();
        String apiVerJsonStr = readApiVersions(apiFilePath);

        // component not pass the apiversion parameter
        if (null == apiVerJsonStr || apiVerJsonStr.isEmpty()) {
          return null;
        }

        // there is no apiVersion filed in apiVerJsonStr or the format is wrong.
        Map<String, Object> apiMap= new Gson().fromJson(apiVerJsonStr, Map.class);
        if (!apiMap.containsKey("apiVersion")) {
          LOGGER.warn("There is no apiVersion config in api file");
          return null;
        }

        // read service or resource api and put in HashMap one by one.
        List<Object> currApiArray = null;
        Map<String, Object> apiVersMap = (apiMap.get("apiVersion") instanceof Map) ? (Map)apiMap.get("apiVersion") : null;
        if(null != apiVersMap)
        {
          for(Map.Entry<String, Object> entry : apiVersMap.entrySet())
          {
            currApiArray = (entry.getValue() instanceof List) ? (List) entry.getValue(): null;
            if (null != currApiArray) {
              List<ApiVersionModel> currApiLst = new ArrayList<ApiVersionModel>();
              currApiArray.forEach(currApi -> {
                currApiLst.add(new ApiVersionModel(currApi.toString()));
              });
              verMap.put(entry.getKey(), currApiLst);
            }
          }
        }
      }
    }

    List<ApiVersionModel> apiLst = verMap.get(requestedApiName);
    return (null == apiLst || apiLst.isEmpty()) ? null : Collections.unmodifiableList(verMap.get(requestedApiName));
  }

  /**
   * read api version from path
   * 
   * @param filePath api version file
   * @return api versions
   * @throws IOException 
   */
  private static String readApiVersions(String filePath){
    try {
      return new String(Files.readAllBytes(Paths.get(filePath)));
    } catch (IOException e) {
      LOGGER.warn("Fail to read api version file", e);
    }
    
    return null;
  }
}
