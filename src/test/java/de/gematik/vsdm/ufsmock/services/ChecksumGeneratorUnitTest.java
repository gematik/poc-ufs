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

import static de.gematik.vsdm.ufsmock.utils.ChecksumValidator.extractKvnrFromChecksum;
import static org.assertj.core.api.Assertions.assertThat;
import java.nio.ByteBuffer;
import java.time.Instant;
import lombok.val;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;

class ChecksumGeneratorUnitTest {

  @Test
  void testUnixTimestampGenerator() {
    ByteBuffer wrapped = ByteBuffer.wrap(CryptoUtils.retrieveShortenedUnixTime());
    val value = wrapped.getInt();
    assertThat(Integer.toString(value))
      .startsWith("17")
      .hasSize(10);
    assertThat(Instant.ofEpochSecond(value))
      .isAfter(Instant.now().minusSeconds(10))
      .isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void testDecryptionWithPythonGeneratedSamples() {
    val key = "0000000000000000000000000000000000000000000000000000000000000001";
    val pruefnachweis = Base64.decode("AUEyVQBnZBo7pWB8xQd5lXpMi64mKvAO32JLCM13LV9mxA1CYlaj4I0awBahK/Q=");
    assertThat(extractKvnrFromChecksum(pruefnachweis, key))
      .isEqualTo("A123456789");
  }
}
