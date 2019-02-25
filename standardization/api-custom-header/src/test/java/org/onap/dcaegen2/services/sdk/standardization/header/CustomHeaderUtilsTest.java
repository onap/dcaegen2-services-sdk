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

import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CustomHeaderUtilsTest {
  private String apiVersions =
      "{\"apiVersion\": {\"eventListener\": [\"4.7.2\",\"5.3.2\",\"5.4.1\",\"7.0.1\"],\"xxxxxx\": [\"1.0.2\",\"1.1.2\",\"2.0.1\"]}}";
  private String filePath = getClass().getClassLoader().getResource("api_version_config.json").getPath();

  @Test
  void testRequestNoMinorVer() {
    CustomHeaderUtils util = getHeaderUtil(null);
    util.isOkCustomHeaders();
    System.out.println("all api: " + apiVersions);
    System.out.print("eventListener/no x-minorversion" + ": ");
    System.out.println(util.getRspCustomHeader().toString());
  }

  @Test
  void testRequestWithMinorVer() {
    CustomHeaderUtils util = getHeaderUtil("2");
    
    // check request header
    util.isOkCustomHeaders();
    
    assertFalse(util.isOkCustomHeaders());
    
    System.out.print("eventListener/wrong x-minorversion 2" + ": ");
    System.out.println(util.getRspCustomHeader().toString());
  }

  @Test
  void testRequestWithMinorVerOk() {
    CustomHeaderUtils util = getHeaderUtil("3");
    assertFalse(!util.isOkCustomHeaders());
    System.out.print("eventListener/x-minorversion 3" + ": ");
    System.out.println(util.getRspCustomHeader().toString());
  }

  /**
   * mock http request
   * 
   * @param uri
   * @param header
   * @return
   */
  private CustomHeaderUtils getHeaderUtil(String minorVer)
  {
    Map <String, String> reqHeaderMap = new HashMap<String, String>();
    reqHeaderMap.put("X-MinorVersion", minorVer);
    return new CustomHeaderUtils("5", reqHeaderMap, filePath, "eventListener");
  }
}
