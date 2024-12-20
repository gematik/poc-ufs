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

import static de.gematik.vsdm.ufsmock.services.CryptoUtils.concatenate;
import static de.gematik.vsdm.ufsmock.services.CryptoUtils.encryptUsingAesGcm;
import static de.gematik.vsdm.ufsmock.services.CryptoUtils.performHkdf;
import static de.gematik.vsdm.ufsmock.services.CryptoUtils.retrieveShortenedUnixTime;
import ch.qos.logback.core.encoder.ByteArrayUtil;
import de.gematik.vsdm.ufsmock.services.config.ChecksumGeneratorConfig;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.Security;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jetty.util.ArrayUtil;
import org.springframework.stereotype.Service;

/**
 * The logic for the generation of the checksum has been originally implemented by erezept Siehe
 * https://gitlab.prod.ccs.gematik.solutions/git/erezept/fachdienst/erp-e2e/-/blob/Development_1.x/konnektor-client/src/main/java/de/gematik/test/konnektor/soap/mock/vsdm/VsdmChecksum.java?ref_type=heads
 * We took this part over.
 */
@Service
@AllArgsConstructor
public class ChecksumGenerator {

  ChecksumGeneratorConfig checksumParams;

  /**
   * The method generate a checksum encode as base64. The checksum contains the payload and the first 24 bytes of the
   * signature ( a HMac hash (SHA256) over the given payload).
   *
   * @return a base64 encoded checksum
   */
  public byte[] generateChecksum(String kvnr) {
    val vsdmPlusAesKey = performHkdf(checksumParams.getKey(), "VSDM+ Version 2 AES/GCM", 16 * 8);
    val shortenedUnixTime = retrieveShortenedUnixTime();

    val myAad = concatenate(checksumParams.getVersion(), checksumParams.getIdentifier(), checksumParams.getKeyVersion(),
      checksumParams.getUpdateReasonIdentifier(), shortenedUnixTime);

    byte[] iv = new byte[12];
    ThreadLocalRandom.current().nextBytes(iv); // NOSONAR

    val ciphertext = encryptUsingAesGcm(kvnr.getBytes(), vsdmPlusAesKey, myAad, iv);

    val vsdmPruefnachweis = concatenate(myAad, iv, ciphertext);

    return vsdmPruefnachweis;
  }
}
