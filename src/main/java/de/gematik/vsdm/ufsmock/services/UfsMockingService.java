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

package de.gematik.vsdm.ufsmock.services;

import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.DEFAULT_GEMATIK_PROVIDER_ID;

import de.gematik.vsdm.ufsmock.domain.ServiceLocalization;
import de.gematik.vsdm.ufsmock.domain.ServiceReceipt;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ws.soap.SoapHeaderElement;

@Slf4j
@Service
@AllArgsConstructor
public class UfsMockingService {

  private ChecksumGenerator checksumGenerator;
  private KvnrFinder kvnrFinder;

  public ServiceReceipt getMockedServiceReceipt(String iccsn,
      SoapHeaderElement serviceLocalizationFromRequestHeader) {
    var receipt = new ServiceReceipt();
    receipt.setServiceLocalization(getServiceLocalization(serviceLocalizationFromRequestHeader));
    receipt.setReceipt(checksumGenerator.generateChecksum(kvnrFinder.getMatchingKvnr(iccsn)));
    return receipt;
  }

  private ServiceLocalization getServiceLocalization(
      SoapHeaderElement serviceLocalizationFromRequestHeader) {
    var serviceLocalization = new ServiceLocalization();
    serviceLocalization.setProvider(retrieveProviderId(serviceLocalizationFromRequestHeader));
    serviceLocalization.setType("UFS");
    return serviceLocalization;
  }

  private String retrieveProviderId(SoapHeaderElement serviceLocalizationFromRequestHeader) {
    try {
      JAXBContext context = JAXBContext.newInstance(ServiceLocalization.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      var serviceLocalization = (ServiceLocalization) unmarshaller.unmarshal(
          serviceLocalizationFromRequestHeader.getSource());
      return serviceLocalization.getProvider();
    } catch (Exception e) {
      log.error(
          "ProviderID could not be retrieved from request header, default Gematik-ProviderID used instead");
      return DEFAULT_GEMATIK_PROVIDER_ID;
    }
  }

}
