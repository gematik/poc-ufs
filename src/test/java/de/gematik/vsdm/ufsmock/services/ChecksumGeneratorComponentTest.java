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


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.vsdm.ufsmock.services.config.ChecksumGeneratorConfig;
import de.gematik.vsdm.ufsmock.services.config.ChecksumGeneratorConfig.InvalidKeyLengthException;
import de.gematik.vsdm.ufsmock.utils.ChecksumValidator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.DisabledIf;

@DisabledIf(expression = "#{environment.acceptsProfiles('systestsDEV', 'systestsRU', 'systestsTU')}", loadContext = true)
class ChecksumGeneratorComponentTest {

  @Nested
  @SpringBootTest(classes = {ChecksumGenerator.class, ChecksumValidator.class})
  @EnableConfigurationProperties(value = ChecksumGeneratorConfig.class)
  class happyPath {

    @Autowired
    ChecksumGenerator underTest;
    @Autowired
    ChecksumValidator checksumValidator;
    @Autowired
    ChecksumGeneratorConfig checksumGeneratorConfig;


    @Test
    @DisplayName("given a kvnr a valid checksum is generated")
    @SneakyThrows
    void generateChecksum_GivenAKvnrAValidChecksumIsGenerated() {
      var kvnr = "X123456798";

      var justBeforeChecksumCreation = Instant.now().minus(1, ChronoUnit.MINUTES);
      var checksum = underTest.generateChecksum(kvnr);

      checksumValidator.assertThatChecksumIsPlausible(checksum, justBeforeChecksumCreation, kvnr);
      assertThat(checksumValidator.extractKvnrFromChecksum(checksum, checksumGeneratorConfig.getHexKey()))
        .isEqualTo(kvnr);
    }
  }

  @Nested
  @SpringBootTest(classes = {ChecksumGenerator.class})
  @EnableConfigurationProperties(value = ChecksumGeneratorConfig.class)
  // we override the key with an invalid value (too short)
  @TestPropertySource(properties = "checksum-params.hex-key=a0b1")
  class keyLengthNotOk {

    @Autowired
    ChecksumGenerator underTest;
    @Autowired
    ChecksumGeneratorConfig checksumGeneratorConfig;

    @Test
    @DisplayName("exception if key length not valid")
    @SneakyThrows
    void generateChecksum_ExceptionIfKeyLengthNotValid() {
      // Preparation
      var kvnr = "X123456798";

      // Execution
      var exception = assertThrows(InvalidKeyLengthException.class,
        () -> underTest.generateChecksum(kvnr));

      // Assertion
      assertEquals("The key used for checksum generation has not the required length of 32 bytes.",
        exception.getMessage());
    }
  }
}
