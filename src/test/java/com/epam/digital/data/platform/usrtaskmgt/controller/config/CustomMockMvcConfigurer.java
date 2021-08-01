package com.epam.digital.data.platform.usrtaskmgt.controller.config;

import com.epam.digital.data.platform.starter.errorhandling.BaseRestExceptionHandler;
import com.epam.digital.data.platform.starter.localization.MessageResolver;
import com.epam.digital.data.platform.usrtaskmgt.config.GeneralConfig;
import com.epam.digital.data.platform.usrtaskmgt.exception.handler.RestExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

@RequiredArgsConstructor
public class CustomMockMvcConfigurer implements MockMvcConfigurer {

  private final MessageResolver messageResolver;

  @Override
  public void afterConfigurerAdded(@NonNull ConfigurableMockMvcBuilder<?> builder) {
    var jacksonBuilder = Jackson2ObjectMapperBuilder.json();
    new GeneralConfig().jackson2ObjectMapperBuilderCustomizer().customize(jacksonBuilder);

    ((StandaloneMockMvcBuilder) builder)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(jacksonBuilder.build()))
        .setControllerAdvice(new RestExceptionHandler(messageResolver),
            new BaseRestExceptionHandler());
  }
}
