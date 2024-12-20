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

import java.util.UUID;
import javax.xml.namespace.QName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

@Slf4j
public class CustomSoapFaultResolver extends SoapFaultMappingExceptionResolver {

  @Override
  protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
    var uuid = UUID.randomUUID();
    SoapFaultDetail detail = fault.addFaultDetail();
    detail.addFaultDetailElement(new QName("uuid")).addText(uuid.toString());
    log.error(uuid.toString(), ex);
  }
}
