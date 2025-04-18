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

import com.epam.digital.data.platform.storage.form.config.RedisStorageConfiguration;
import com.epam.digital.data.platform.storage.form.factory.StorageServiceFactory;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@ConditionalOnProperty(prefix = "storage.form-data", name = "type", havingValue = "redis")
public class RedisFormDataStorageConfig {

  @Bean
  public StorageServiceFactory storageServiceFactory(ObjectMapper objectMapper) {
    return new StorageServiceFactory(objectMapper);
  }

  @Bean
  @ConfigurationProperties(prefix = "storage.backend.redis")
  public RedisStorageConfiguration redisStorageConfiguration() {
    return new RedisStorageConfiguration();
  }

  @Bean
  public FormDataStorageService<?> formDataStorageService(StorageServiceFactory factory,
      RedisConnectionFactory redisConnectionFactory) {
    return factory.formDataStorageService(redisConnectionFactory);
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory(StorageServiceFactory factory,
      RedisStorageConfiguration config) {
    return factory.redisConnectionFactory(config);
  }
}
