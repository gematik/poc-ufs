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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.vsdm.ufsmock.data.EhcaClient;
import de.gematik.vsdm.ufsmock.domain.Egk;
import de.gematik.vsdm.ufsmock.services.KvnrFinder.KvnrFinderException;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.DisabledIf;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestEntityManager
@DisabledIf(expression = "#{environment.acceptsProfiles('systestsDEV', 'systestsRU', 'systestsTU')}", loadContext = true)
class KvnrFinderIT {

  @Autowired
  private KvnrFinder underTest;
  @Autowired
  private TestEntityManager testEntityManager;
  @MockBean
  private EhcaClient ehcaClient;
  @Autowired
  EgkCache cache;

  @BeforeEach
  void clearCache() {
    cache.clear();
  }


  @Test
  @DisplayName("kvnr retrieved from cache if possible")
  @SneakyThrows
  @Transactional
  void getMatchingKvnr_KvnrRetrievedFromCacheIfPossible() {
    // Preparation
    var iccsn = "12121212121212";
    var kvnr = "X123456789";
    testEntityManager.persist(new Egk(iccsn, kvnr));

    // Execution
    var result = underTest.getMatchingKvnr(iccsn);

    // Assertion
    assertThat(result, is(kvnr));
  }

  @Test
  @DisplayName("kvnr fetched from EHCA if nothing in cache")
  @SneakyThrows
  @Transactional
  void getMatchingKvnr_KvnrRetrievedFromEhca() {
    // Preparation
    var iccsn = "12121212121212";
    var kvnr = "X123456789";
    Mockito.when(ehcaClient.fetchKvnr(iccsn)).thenReturn(Optional.of(kvnr));

    // Execution
    var result = underTest.getMatchingKvnr(iccsn);

    // Assertion
    assertAll(
        () -> assertThat(result, is(kvnr)),
        () -> assertTrue(fetchedKvnrIsAddedToCache(new Egk(iccsn, result)))
    );
  }

  private boolean fetchedKvnrIsAddedToCache(Egk egk) {
    var result = testEntityManager.find(Egk.class, egk.getIccsn());
    return Objects.equals(result.getKvnr(), egk.getKvnr());
  }


  @Test
  @DisplayName("Exception thrown if no Kvnr cannot be retrieved")
  @SneakyThrows
  @Transactional
  void getMatchingKvnr_ExceptionIfNoKvnrCannotBeRetrieved() {
    // Preparation
    var iccsn = "12121212121212";
    Mockito.when(ehcaClient.fetchKvnr(iccsn)).thenReturn(Optional.empty());

    // Execution and assertion
    var exception = Assertions.assertThrows(KvnrFinderException.class,
        () -> underTest.getMatchingKvnr(iccsn));
    assertThat(exception.getMessage(),
        is("No KVNR could be retrieved based on ICCSN 12121212121212"));
  }

}
