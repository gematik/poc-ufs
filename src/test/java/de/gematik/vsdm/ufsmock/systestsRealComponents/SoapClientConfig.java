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

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;


@Configuration
@Profile({"systestsDEV", "systestsRU", "systestsTU"})
public class SoapClientConfig {


  @Value("${testobject.url}")
  private String targetUrl;


  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setPackagesToScan("de.gematik.vsdm.ufsmock.domain");
    return marshaller;
  }


  @Bean
  @SneakyThrows
  public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller,
      SslBundles sslBundles) {
    WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
    webServiceTemplate.setMarshaller(marshaller);
    webServiceTemplate.setUnmarshaller(marshaller);
    webServiceTemplate.setDefaultUri(targetUrl);
    HttpsUrlConnectionMessageSender sender = new HttpsUrlConnectionMessageSender();
    sender.setTrustManagers(sslBundles.getBundle("server").getManagers().getTrustManagers());
    webServiceTemplate.setMessageSender(sender);
    return webServiceTemplate;
  }

}
