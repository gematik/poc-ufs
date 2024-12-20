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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.vsdm.ufsmock.data.EhcaClient.KvnrSearchResultDTO;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisabledIf(expression = "#{environment.acceptsProfiles('systestsDEV', 'systestsRU', 'systestsTU')}", loadContext = true)
class EhcaClientWithMockedServerTest {

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private EhcaClient underTest;

  @Value("${ehca.fqdn}")
  private String ehcaFqdn;

  private MockRestServiceServer mockRestServiceServer;

  @PostConstruct
  void init() {
    mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
  }


  @Test
  void fetchKvnr_kvnrIsFound() {

    //Preparation
    var iccsn = "12121212121212";
    var kvnr = "X123456789";
    var result = new KvnrSearchResultDTO(iccsn, kvnr);

    mockRestServiceServer.expect(requestTo(ehcaFqdn + "/rest/iccsn/12121212121212/kvnr"))
        .andRespond(withSuccess(mapToJson(result), MediaType.APPLICATION_JSON));

    //Execution
    var expectedKvnr = underTest.fetchKvnr(iccsn);

    //Assertion
    mockRestServiceServer.verify();
    assertAll(
        () -> assertThat(expectedKvnr, is(Optional.of(kvnr))));
  }


  @Test
  void fetchKvnr_kvnrIsNotFound() {

    //Preparation
    var iccsn = "12121212121212";

    mockRestServiceServer
        .expect(requestTo(ehcaFqdn + "/rest/iccsn/12121212121212/kvnr"))
        .andRespond(withResourceNotFound());

    //Execution
    var expectedKvnr = underTest.fetchKvnr(iccsn);

    //Assertion
    mockRestServiceServer.verify();
    assertAll(() -> assertThat(expectedKvnr, is(Optional.empty())));
  }


  @SneakyThrows
  private static String mapToJson(Object object) {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(object);
  }

}
