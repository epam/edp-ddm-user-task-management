/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.usrtaskmgt.config;

import com.epam.digital.data.platform.integration.ceph.config.S3ConfigProperties;
import com.epam.digital.data.platform.integration.ceph.factory.CephS3Factory;
import com.epam.digital.data.platform.storage.form.config.CephStorageConfiguration;
import com.epam.digital.data.platform.storage.form.factory.StorageServiceFactory;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The configurations that contains and configures beans for form data storage service.
 */
@Configuration
@ConditionalOnProperty(prefix = "storage.form-data", name = "type", havingValue = "ceph")
public class CephFormDataStorageConfig {

  @Bean
  @ConfigurationProperties(prefix = "s3.config")
  public S3ConfigProperties s3ConfigProperties() {
    return new S3ConfigProperties();
  }

  @Bean
  public CephS3Factory cephS3Factory() {
    return new CephS3Factory(s3ConfigProperties());
  }

  @Bean
  public StorageServiceFactory storageServiceFactory(
      ObjectMapper objectMapper, CephS3Factory cephS3Factory) {
    return new StorageServiceFactory(objectMapper, cephS3Factory);
  }

  @Bean
  @ConfigurationProperties(prefix = "storage.backend.ceph")
  public CephStorageConfiguration cephStorageConfiguration() {
    return new CephStorageConfiguration();
  }

  @Bean
  public FormDataStorageService<?> formDataStorageService(StorageServiceFactory factory,
      CephStorageConfiguration config) {
    return factory.formDataStorageService(config);
  }
}
