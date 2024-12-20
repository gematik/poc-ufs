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

package de.gematik.vsdm.ufsmock.systestsRealComponents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.vsdm.ufsmock.domain.GetUpdateFlags;
import de.gematik.vsdm.ufsmock.domain.GetUpdateFlagsResponse;
import de.gematik.vsdm.ufsmock.utils.ChecksumValidator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;


/**
 * This test ensures that the productive UFS-Mock is reachable and works properly
 */
@EnabledIf(expression = "#{environment.acceptsProfiles('systestsDEV', 'systestsRU', 'systestsTU')}", loadContext = true)
@SpringBootTest
public class SystemIT {

  private final ChecksumValidator checksumValidator;

  private final WebServiceTemplate webServiceTemplate;

  private final String existingIccsn;

  private final String matchingKvnr;


  @Autowired
  public SystemIT(WebServiceTemplate webServiceTemplate,
      @Value("${testobject.existing-iccsn}") String validIccsn,
      @Value("${testobject.matching-kvnr}") String matchingKvnr,
      ChecksumValidator checksumValidator) {
    this.webServiceTemplate = webServiceTemplate;
    this.existingIccsn = validIccsn;
    this.matchingKvnr = matchingKvnr;
    this.checksumValidator = checksumValidator;
  }


  @Test
  @DisplayName("UFS Mock in target environment is working as expected")
  @SneakyThrows
  void sendGetUpdateFlagsRequestToUFSMock_iccsn_existing() {
    // Preparation
    GetUpdateFlags request = new GetUpdateFlags();
    request.setIccsn(existingIccsn);
    var justBeforeChecksumCreation = Instant.now().minus(1, ChronoUnit.MINUTES);

    // Execution
    GetUpdateFlagsResponse response = (GetUpdateFlagsResponse) webServiceTemplate.marshalSendAndReceive(
        request);

    var checksum = response.getServiceReceipt().getFirst().getReceipt();

    // Assertion
    assertAll(
        () -> assertNotNull(response),
        () -> assertThat(response.getUpdateFlag(), Matchers.empty()),
        () -> assertThat(response.getServiceReceipt(), Matchers.hasSize(1)),
        () -> checksumValidator.assertThatChecksumIsPlausible(
            checksum,
            justBeforeChecksumCreation,
            matchingKvnr
        )
    );
  }

  @Test
  @DisplayName("Exception if iccsn not found")
  @SneakyThrows
  void sendGetUpdateFlagsRequestToUFSMock_iccsn_not_existing() {
    // Preparation
    var notAvailableIccsn = "80276881048000813490";
    GetUpdateFlags request = new GetUpdateFlags();
    request.setIccsn(notAvailableIccsn);

    // Execution and Assertion
    try {
      webServiceTemplate.marshalSendAndReceive(request);
    } catch (SoapFaultClientException e) {
      assertAll(
          () -> assertThat(e.getMessage(), is("Gematik UFS-Mock Verarbeitungsfehler"))
      );
    }
  }

}
