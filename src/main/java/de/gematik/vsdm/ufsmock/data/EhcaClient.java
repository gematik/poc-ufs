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

package de.gematik.vsdm.ufsmock.data;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
@Slf4j
public class EhcaClient {

  private final RestTemplate restTemplate;

  public Optional<String> fetchKvnr(String iccsn) {
    var message = "Call to EHCA to retrieve KVNR for ICCSN " + iccsn + ". Result: ";
    try {
      var response = restTemplate.getForEntity("/iccsn/{iccsn}/kvnr", KvnrSearchResultDTO.class,
          iccsn);
      var kvnr = Optional.ofNullable(response.getBody()).map(KvnrSearchResultDTO::getKvnr);
      message += kvnr.orElse("not found");
      return kvnr;
    } catch (HttpStatusCodeException e) {
      message += e.getStatusCode();
      return Optional.empty();
    } catch (Exception e) {
      message += e.getMessage();
      return Optional.empty();
    } finally {
      log.info(message);
    }
  }

  //  Copied from dependency 'ehca-commons'
//  in order to avoid many transitive dependencies, some of them with CVE
  @Data
  @AllArgsConstructor
  public static class KvnrSearchResultDTO {

    private String iccsn;
    private String kvnr;
  }
}
