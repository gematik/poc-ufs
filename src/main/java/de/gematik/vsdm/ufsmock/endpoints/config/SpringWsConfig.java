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

package de.gematik.vsdm.ufsmock.endpoints.config;

import java.util.List;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.XsdSchemaCollection;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

@EnableWs
@Configuration
public class SpringWsConfig extends WsConfigurerAdapter {


  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, "/ufs/*");
  }

  // Expose the WSDL over: {host}/ufs/{beanName}.wsdl
  @Bean(name = "UfsMockService")
  public Wsdl11Definition defaultWsdl11Definition() {
    SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
    wsdl11Definition.setWsdl(new ClassPathResource("/soapContract/wsdl/uf/UFS.wsdl"));
    return wsdl11Definition;
  }

  //Check the schema-conformity of the request
  @Override
  public void addInterceptors(List<EndpointInterceptor> interceptors) {
    CustomValidationInterceptor validatingInterceptor = new CustomValidationInterceptor();
    validatingInterceptor.setValidateRequest(true);
    validatingInterceptor.setXsdSchemaCollection(schemas());
    interceptors.add(validatingInterceptor);
  }

  @Bean
  public XsdSchemaCollection schemas() {
    CommonsXsdSchemaCollection xsds = new CommonsXsdSchemaCollection(
        new ClassPathResource("soapContract/wsdl/common/CmCommon.xsd"),
        new ClassPathResource("soapContract/wsdl/uf/CmUfServiceRequest.xsd")
    );
    xsds.setInline(true);
    return xsds;
  }

  @Bean
  public SoapFaultMappingExceptionResolver exceptionResolver() {
    SoapFaultMappingExceptionResolver exceptionResolver = new CustomSoapFaultResolver();
    SoapFaultDefinition faultDefinition = new SoapFaultDefinition();
    faultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
    faultDefinition.setFaultStringOrReason("Gematik UFS-Mock Verarbeitungsfehler");
    exceptionResolver.setDefaultFault(faultDefinition);
    exceptionResolver.setOrder(1);
    return exceptionResolver;
  }
}
