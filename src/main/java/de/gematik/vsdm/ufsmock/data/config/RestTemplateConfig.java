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

package de.gematik.vsdm.ufsmock.data.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {


  @Bean(name = "ehcaRestTemplate")
  public RestTemplate createEhcaRestTemplate(RestTemplateBuilder restTemplateBuilder,
      @Value("${ehca.fqdn}") String ehcaFqdn, SslBundles sslBundles) {
    return restTemplateBuilder
        .setSslBundle(sslBundles.getBundle("ehca"))
        .rootUri(ehcaFqdn + "/rest")
        .build();
  }

}
