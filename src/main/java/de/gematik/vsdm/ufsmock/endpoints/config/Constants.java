/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.vsdm.ufsmock.endpoints.config;

public class Constants {

  private Constants() {
  }

  public static final String NAMESPACE_REQUEST = "http://ws.gematik.de/cm/uf/CmUfServiceRequest/v2.0";
  public static final String NAMESPACE_RESPONSE = "http://ws.gematik.de/cm/uf/CmUfServiceResponse/v2.0";
  public static final String NAMESPACE_COMMON = "http://ws.gematik.de/cm/common/CmCommon/v2.0";
  public static final String NAMESPACE_ENVELOPPE = "http://schemas.xmlsoap.org/soap/envelope/";
  public static final String DEFAULT_GEMATIK_PROVIDER_ID = "109500969";

}
