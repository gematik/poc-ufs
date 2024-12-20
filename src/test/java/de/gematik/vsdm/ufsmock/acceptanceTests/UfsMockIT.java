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

package de.gematik.vsdm.ufsmock.acceptanceTests;

import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.DEFAULT_GEMATIK_PROVIDER_ID;
import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.NAMESPACE_COMMON;
import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.NAMESPACE_ENVELOPPE;
import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.NAMESPACE_REQUEST;
import static de.gematik.vsdm.ufsmock.endpoints.config.Constants.NAMESPACE_RESPONSE;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.RequestCreators.withSoapEnvelope;
import static org.springframework.ws.test.server.ResponseMatchers.clientOrSenderFault;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
import static org.springframework.ws.test.server.ResponseMatchers.serverOrReceiverFault;
import static org.springframework.ws.test.server.ResponseMatchers.xpath;

import de.gematik.vsdm.ufsmock.data.EhcaClient;
import de.gematik.vsdm.ufsmock.services.EgkCache;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.webservices.server.AutoConfigureMockWebServiceClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.ws.test.server.ResponseMatchers;
import org.springframework.xml.transform.StringSource;

@SpringBootTest
@AutoConfigureMockWebServiceClient
@AutoConfigureTestEntityManager
@DisabledIf(expression = "#{environment.acceptsProfiles('systestsDEV', 'systestsRU', 'systestsTU')}", loadContext = true)
class UfsMockIT {

  @Autowired
  private MockWebServiceClient client;

  @MockBean
  EhcaClient ehcaClient;

  @Autowired
  EgkCache cache;

  @BeforeEach
  void clearCache() {
    cache.clear();
  }


  private static final Map<String, String> NAMESPACE_MAPPING = Map.ofEntries(
      Map.entry("ns2", NAMESPACE_COMMON),
      Map.entry("ns3", NAMESPACE_RESPONSE),
      Map.entry("SOAP-ENV", NAMESPACE_ENVELOPPE)
  );


  @Test
  @DisplayName("server returns error if request not schema conform")
  @SneakyThrows
  void serverReturnsErrorIfRequestNotSchemaConform() {
    // Preparation
    StringSource invalidRequest =
        new StringSource(
            "<UFS:GetUpdateFlags xmlns:UFS='" + NAMESPACE_REQUEST + "'>\n"
                + "      <CM:Iccsn xmlns:CM='" + NAMESPACE_COMMON + "'>XXX</CM:Iccsn>\n"
                + "    </UFS:GetUpdateFlags>"
        );

    // Execution
    var result = client.sendRequest(withPayload(invalidRequest));

    // Assertion
    Assertions.assertAll(
        () -> result.andExpect(clientOrSenderFault())
    );
  }

  @Test
  @DisplayName("Server returns expected response, use ProviderId from header if available")
  @SneakyThrows
  void serverReturnsExpectedResponse_ProviderIdAvailableInHeader() {
    // Preparation
    var randomProviderId = "888888888";
    final var iccsn = "80276881048000813492";
    StringSource validRequest = new StringSource(
        "<soapenv:Envelope xmlns:ABC=\"http://ws.gematik.de/cm/common/CmCommon/v2.0\" xmlns:CM=\"http://ws.gematik.de/cm/common/CmCommon/v2.0\" xmlns:UFS=\"http://ws.gematik.de/cm/uf/CmUfServiceRequest/v2.0\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soapenv:Header xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
            + "    <CM:ServiceLocalization >\n"
            + "      <CM:Type>UFS</CM:Type>\n"
            + "      <CM:Provider>" + randomProviderId + "</CM:Provider>\n"
            + "    </CM:ServiceLocalization>\n"
            + "  </soapenv:Header>\n"
            + "<soapenv:Body>\n"
            + "<UFS:GetUpdateFlags >\n"
            + "      <CM:Iccsn>" + iccsn + "</CM:Iccsn>\n"
            + "    </UFS:GetUpdateFlags>\n"
            + " </soapenv:Body>\n"
            + "</soapenv:Envelope>"
    );
    Mockito.when(ehcaClient.fetchKvnr(iccsn)).thenReturn(Optional.of("randomKvnr"));

    // Execution
    var result = client.sendRequest(withSoapEnvelope(validRequest));

    // Assertion
    Assertions.assertAll(
        () -> result.andExpect(noFault()),
        () -> result.andExpect(
            xpath(
                "/ns3:GetUpdateFlagsResponse/ns2:ServiceReceipt/ns2:ServiceLocalization/ns2:Provider",
                NAMESPACE_MAPPING).evaluatesTo(randomProviderId)),
        () -> result.andExpect(ResponseMatchers.validPayload(
            (new ClassPathResource("soapContract/wsdl/uf/CmUfServiceResponse.xsd"))))
    );
  }


  @Test
  @DisplayName("Server returns expected response, use default ProviderId if not available in header")
  @SneakyThrows
  void serverReturnsExpectedResponse_useDefaultProviderIdIfNotAvailableInHeader() {
    // Preparation
    final var iccsn = "80276881048000813492";
    StringSource validRequest = new StringSource(
        "    <UFS:GetUpdateFlags xmlns:UFS='" + NAMESPACE_REQUEST + "'>\n"
            + "      <CM:Iccsn xmlns:CM='" + NAMESPACE_COMMON
            + "'>" + iccsn + "</CM:Iccsn>\n"
            + "    </UFS:GetUpdateFlags>"
    );
    Mockito.when(ehcaClient.fetchKvnr(iccsn)).thenReturn(Optional.of("randomKvnr"));

    // Execution
    var result = client.sendRequest(withPayload(validRequest));

    // Assertion
    Assertions.assertAll(
        () -> result.andExpect(noFault()),
        () -> result.andExpect(
            xpath(
                "/ns3:GetUpdateFlagsResponse/ns2:ServiceReceipt/ns2:ServiceLocalization/ns2:Provider",
                NAMESPACE_MAPPING).evaluatesTo(DEFAULT_GEMATIK_PROVIDER_ID)),
        () -> result.andExpect(ResponseMatchers.validPayload(
            (new ClassPathResource("soapContract/wsdl/uf/CmUfServiceResponse.xsd"))))
    );
  }


  @Test
  @DisplayName("server returns error if kvnr can not be found")
  @SneakyThrows
  void serverReturnsErrorIfKvnrCanNotBeFound() {
    // Preparation
    final var iccsn = "80276881048000813492";
    StringSource validRequest = new StringSource(
        "    <UFS:GetUpdateFlags xmlns:UFS='" + NAMESPACE_REQUEST + "'>\n"
            + "      <CM:Iccsn xmlns:CM='" + NAMESPACE_COMMON
            + "'>" + iccsn + "</CM:Iccsn>\n"
            + "    </UFS:GetUpdateFlags>"
    );
    Mockito.when(ehcaClient.fetchKvnr(iccsn)).thenReturn(Optional.empty());

    // Execution
    var result = client.sendRequest(withPayload(validRequest));

    // Assertion
    Assertions.assertAll(
        () -> result.andExpect(
            serverOrReceiverFault("Gematik UFS-Mock Verarbeitungsfehler")),
        () -> result.andExpect(
            xpath("/SOAP-ENV:Fault/detail/uuid", NAMESPACE_MAPPING).exists())
    );
  }


  @Test
  @DisplayName("in case of any exception an uuid is returned")
  @SneakyThrows
  void ifExceptionUuidIsAlwaysReturned() {
    // Preparation
    final var iccsn = "80276881048000813492";
    StringSource validRequest = new StringSource(
        "    <UFS:GetUpdateFlags xmlns:UFS='" + NAMESPACE_REQUEST + "'>\n"
            + "      <CM:Iccsn xmlns:CM='" + NAMESPACE_COMMON
            + "'>" + iccsn + "</CM:Iccsn>\n"
            + "    </UFS:GetUpdateFlags>"
    );
    Mockito.when(ehcaClient.fetchKvnr(iccsn)).thenAnswer((t) -> {
      throw new Exception();
    });

    // Execution
    var result = client.sendRequest(withPayload(validRequest));

    // Assertion
    Assertions.assertAll(
        () -> result.andExpect(
            serverOrReceiverFault("Gematik UFS-Mock Verarbeitungsfehler")),
        () -> result.andExpect(
            xpath("/SOAP-ENV:Fault/detail/uuid", NAMESPACE_MAPPING).exists())
    );
  }


}
