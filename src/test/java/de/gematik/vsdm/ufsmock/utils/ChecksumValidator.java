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

package de.gematik.vsdm.ufsmock.utils;

import static de.gematik.vsdm.ufsmock.services.CryptoUtils.decryptUsingAesGcm;
import static de.gematik.vsdm.ufsmock.services.CryptoUtils.performHkdf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.vsdm.ufsmock.services.config.ChecksumGeneratorConfig;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChecksumValidator {

  @Autowired
  ChecksumGeneratorConfig checksumGeneratorConfig;

  public void assertThatChecksumIsPlausible(byte[] checksum,
    Instant justBeforeChecksumCreation,
    String initialKvnr) {
/*    val kvnr = copyByteArrayFrom(checksum, 0, 10);
    val timestamp = Instant.ofEpochSecond(
        Long.parseLong(
            new String(copyByteArrayFrom(checksum, 10, 20), StandardCharsets.UTF_8)));
    val reason = copyCharFrom(checksum, 20);
    val identifier = copyCharFrom(checksum, 21);
    val version = copyCharFrom(checksum, 22);

    assertAll(
        () -> assertEquals(kvnr, initialKvnr, "check kvnr"),
        () -> assertTrue(
            timestamp.isAfter(justBeforeChecksumCreation), "check timestamp"),
        () -> assertEquals(checksumGeneratorConfig.getUpdateReasonIdentifier(), reason,
            "check reason"),
        () -> assertEquals(checksumGeneratorConfig.getIdentifier(), identifier,
            "check identifier"),
        () -> assertEquals(checksumGeneratorConfig.getVersion(), version, "check version"));*/
  }

  private static byte[] copyByteArrayFrom(byte[] data, int from, int to) {
    return Arrays.copyOfRange(data, from, to);
  }

  private static char copyCharFrom(byte[] data, int pos) {
    return (char) data[pos];
  }

  public static String extractKvnrFromChecksum(byte[] checksum, String hexEncodedKey) {
    val key = Hex.decode(hexEncodedKey);
    val vsdmPlusAesKey = performHkdf(key, "VSDM+ Version 2 AES/GCM", 16 * 8);
    val versionNumber = checksum[0];
    val vsdmBetreiberKennung = checksum[1];
    val vsdmKeyVersion = checksum[2];
    val vsdmUpdateReason = checksum[3];
    val vsdmTimestamp = convertToInstant(Arrays.copyOfRange(checksum, 4, 9));
    val vsdmIv = Arrays.copyOfRange(checksum, 9, 21);
    val vsdmCiphertext = Arrays.copyOfRange(checksum, 21, checksum.length);
    val kvnr = decryptUsingAesGcm(vsdmCiphertext, vsdmPlusAesKey, Arrays.copyOfRange(checksum, 0, 9), vsdmIv);
    return new String(kvnr, StandardCharsets.UTF_8);
  }

  private static Object convertToInstant(byte[] bytes) {
    ByteBuffer.wrap(bytes).getInt();
    return null;
  }
}
