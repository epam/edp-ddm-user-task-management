package com.epam.digital.data.platform.usrtaskmgt.model;

import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import org.springframework.http.HttpMethod;

@Builder
@Getter
public class StubRequest {

  private final String path;
  private final HttpMethod method;
  @Default
  private final Map<String, StringValuePattern> queryParams = Map.of();
  @Default
  private final Map<String, List<StringValuePattern>> requestHeaders = Map.of();
  private final ContentPattern<String> requestBody;

  private final int status;
  private final String responseBody;
  @Default
  private final Map<String, List<String>> responseHeaders = Map.of();
}
