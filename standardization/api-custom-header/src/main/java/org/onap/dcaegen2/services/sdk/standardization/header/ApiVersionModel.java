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

public class ApiVersionModel {
  private String majorVersion;
  private String minorVersion;
  private String patchVersion;
  private String version;

  public ApiVersionModel(String version) {
    this.version = version;

    String[] verArray = version.split("\\.");
    majorVersion = verArray[0];
    minorVersion = verArray.length > 1 ? verArray[1] : null;
    patchVersion = verArray.length > 2 ? verArray[2] : null;
  }

  public String getMajorVersion() {
    return majorVersion;
  }

  public String getMinorVersion() {
    return minorVersion;
  }

  public String getPatchVersion() {
    return patchVersion;
  }

  public String getVersion() {
    return version;
  }
}
