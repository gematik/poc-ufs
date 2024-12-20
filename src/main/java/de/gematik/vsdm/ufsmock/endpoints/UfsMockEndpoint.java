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

package de.gematik.vsdm.ufsmock.endpoints;

import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.NAMESPACE_COMMON;
import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.NAMESPACE_REQUEST;

import de.gematik.vsdm.ufsmock.domain.GetUpdateFlags;
import de.gematik.vsdm.ufsmock.domain.GetUpdateFlagsResponse;
import de.gematik.vsdm.ufsmock.services.UfsMockingService;
import lombok.AllArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;


@Endpoint
@AllArgsConstructor
public class UfsMockEndpoint {

  private final UfsMockingService ufsMockService;

  @PayloadRoot(namespace = NAMESPACE_REQUEST, localPart = "GetUpdateFlags")
  @ResponsePayload
  public GetUpdateFlagsResponse getUpdateFlagsResponse(@RequestPayload GetUpdateFlags request,
      @SoapHeader("{" + NAMESPACE_COMMON
          + "}ServiceLocalization") SoapHeaderElement serviceLocalizationHeader
  ) {
    GetUpdateFlagsResponse response = new GetUpdateFlagsResponse();
    response.getServiceReceipt().add(ufsMockService.getMockedServiceReceipt(request.getIccsn(),
        serviceLocalizationHeader));
    return response;
  }
}
