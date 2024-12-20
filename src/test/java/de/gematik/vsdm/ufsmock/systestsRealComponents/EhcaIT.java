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
import static org.junit.jupiter.api.Assertions.assertAll;

import de.gematik.vsdm.ufsmock.data.EhcaClient;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.EnabledIf;


/**
 * This test makes sure that the productive EHCA is reachable and working as expected. Within
 * {@link SystemIT} a call to the EHCA is already made for the mapping iccsn to kvnr. But since the
 * result of the mapping is then cached, the EHCA is called only the first time and then not
 * anymore. That's why this complementary test was necessary.
 */
@EnabledIf(expression = "#{environment.acceptsProfiles('systestsDEV', 'systestsRU', 'systestsTU')}", loadContext = true)
@SpringBootTest
public class EhcaIT {

  private final EhcaClient underTest;
  private final String existingIccsn;
  private final String matchingKvnr;


  @Autowired
  public EhcaIT(EhcaClient underTest,
      @Value("${testobject.existing-iccsn}") String existingIccsn,
      @Value("${testobject.matching-kvnr}") String matchingKvnr) {
    this.underTest = underTest;
    this.existingIccsn = existingIccsn;
    this.matchingKvnr = matchingKvnr;
  }

  @Test
  void fetchKvnr_communicationWithEhcaSuccessful() {

    //Execution
    var result = underTest.fetchKvnr(existingIccsn);

    //Assertion
    assertAll(() -> assertThat(result, Matchers.is(Optional.of(matchingKvnr))));
  }

}
