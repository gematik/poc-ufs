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

import de.gematik.vsdm.ufsmock.data.EhcaClient;
import de.gematik.vsdm.ufsmock.domain.Egk;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class KvnrFinder {

  private EgkCache egkCache;

  private EhcaClient ehcaClient;

  public String getMatchingKvnr(String iccsn) {
    return egkCache.retrieveEgk(iccsn)
        .map(Egk::getKvnr)
        .orElseGet(() -> retrieveKvnrFromEhca(iccsn));
  }

  private String retrieveKvnrFromEhca(String iccsn) {
    var kvnr = ehcaClient.fetchKvnr(iccsn);
    kvnr.ifPresent(it -> egkCache.add(new Egk(iccsn, it)));
    return kvnr.orElseThrow(
        () -> new KvnrFinderException(iccsn));
  }


  public static class KvnrFinderException extends RuntimeException {

    public KvnrFinderException(String iccsn) {
      super(String.format("No KVNR could be retrieved based on ICCSN %s", iccsn));
    }
  }

}
