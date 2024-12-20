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

package de.gematik.vsdm.ufsmock.services.config;

import javax.xml.bind.DatatypeConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "checksum-params")
@Value
public class ChecksumGeneratorConfig {

  char version;
  char identifier;
  char updateReasonIdentifier;
  char keyVersion;
  String hexKey;


  public byte[] getKey() {
    var key = DatatypeConverter.parseHexBinary(hexKey);
    var expectedKeyLength = 32;
    if (key.length != expectedKeyLength) {
      throw new InvalidKeyLengthException(expectedKeyLength);
    }
    return key;
  }

  public static class InvalidKeyLengthException extends RuntimeException {

    public InvalidKeyLengthException(int expectedLength) {
      super(String.format(
          "The key used for checksum generation has not the required length of %s bytes.",
          expectedLength));
    }
  }
}
