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
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomHeaderUtilsTest {
  private String filePath = getClass().getClassLoader().getResource("api_version_config.json").getPath();

  /**
   * not send minor version when client requests
   */
  @Test
  void testRequestNoMinorVer() {
    CustomHeaderUtils util = getHeaderUtil(null);
    util.isOkCustomHeaders();

    Map<String, String> rspHeaders = util.getRspCustomHeader();
    Assertions.assertTrue("3".equals(rspHeaders.get("X-MinorVersion")));
  }

  /**
   * minor version not exist which client request
   */
  @Test
  void testRequestWithWrongMinorVer() {
    CustomHeaderUtils util = getHeaderUtil("2");

    // check request header
    util.isOkCustomHeaders();

    Assertions.assertFalse(util.isOkCustomHeaders());

    Map<String, String> rspHeaders = util.getRspCustomHeader();
    Assertions.assertTrue("4".equals(rspHeaders.get("X-MinorVersion")));
  }

  /**
   * minor version exists which client request
   */
  @Test
  void testRequestWithMinorVerOk() {
    CustomHeaderUtils util = getHeaderUtil("3");
    Assertions.assertFalse(!util.isOkCustomHeaders());

    Map<String, String> rspHeaders = util.getRspCustomHeader();
    Assertions.assertTrue("3".equals(rspHeaders.get("X-MinorVersion")));
  }

  private CustomHeaderUtils getHeaderUtil(String minorVer) {
    Map<String, String> reqHeaderMap = new HashMap<String, String>();
    reqHeaderMap.put("X-MinorVersion", minorVer);
    return new CustomHeaderUtils("5", reqHeaderMap, filePath, "eventListener");
  }
}
