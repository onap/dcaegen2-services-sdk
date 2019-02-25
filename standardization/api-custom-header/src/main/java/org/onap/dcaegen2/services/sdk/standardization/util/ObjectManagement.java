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
package org.onap.dcaegen2.services.sdk.standardization.util;


import java.util.Collection;

public class ObjectManagement {

  /**
   * check string object if it is empty
   * 
   * @param str string object
   * @return true when str is null or empty
   */
  public static boolean isStringEmpty(String str) {
    return null == str || str.isEmpty();
  }

  /**
   * check collection object if it is empty
   * 
   * @param c collection object
   * @return true when c is null or empty
   */
  public static boolean isCollectionEmpty(Collection<?> c) {
    return null == c || c.isEmpty();
  }
}
