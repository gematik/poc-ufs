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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.Security;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtils {

  private static final Provider provider = new BouncyCastleProvider();

  static {
    Security.addProvider(provider);
  }

  public static byte[] retrieveShortenedUnixTime() {
    return ByteBuffer.allocate(5).putInt((int) Instant.now().getEpochSecond()).array();
  }

  public static byte[] performHkdf(byte[] ikm, String info, int length) {
    HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
    hkdf.init(new HKDFParameters(ikm, null, info.getBytes(StandardCharsets.UTF_8)));
    byte[] okm = new byte[length / 8];
    hkdf.generateBytes(okm, 0, length / 8);
    return okm;
  }

  @SneakyThrows
  public static byte[] encryptUsingAesGcm(byte[] input, byte[] key, byte[] associatedData, byte[] iv) {
    SecretKey secretKey = new SecretKeySpec(key, "AES");
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", provider);

    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(16 * 8, iv));

    if (associatedData != null) {
      cipher.updateAAD(associatedData);
    }

    return cipher.doFinal(input);
  }

  @SneakyThrows
  public static byte[] decryptUsingAesGcm(byte[] cipherText, byte[] key, byte[] associatedData, byte[] iv) {
    SecretKey secretKey = new SecretKeySpec(key, "AES");
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", provider);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(16 * 8, iv));

    if (associatedData != null) {
      cipher.updateAAD(associatedData);
    }

    return cipher.doFinal(cipherText);
  }

  public static byte[] concatenate(Object... args) {
    return Stream.of(args)
      .filter(Objects::nonNull)
      .map(arg -> switch (arg) {
        case Character c -> c.toString().getBytes(StandardCharsets.UTF_8);
        case String s -> s.getBytes(StandardCharsets.UTF_8);
        case byte[] bytes -> bytes;
        case null, default -> throw new IllegalArgumentException("Unsupported type: " + arg.getClass());
      })
      .reduce(new byte[0], CryptoUtils::addByteArrays);
  }

  private static byte[] addByteArrays(byte[] array1, byte[] array2) {
    byte[] result = new byte[array1.length + array2.length];
    System.arraycopy(array1, 0, result, 0, array1.length);
    System.arraycopy(array2, 0, result, array1.length, array2.length);
    return result;
  }
}
