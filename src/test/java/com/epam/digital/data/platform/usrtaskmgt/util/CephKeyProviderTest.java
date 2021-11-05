package com.epam.digital.data.platform.usrtaskmgt.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CephKeyProviderTest {

  private CephKeyProvider cephKeyProvider;

  @BeforeEach
  public void init() {
    cephKeyProvider = new CephKeyProvider();
  }

  @Test
  void testGeneratingCephKey() {
    var expectedKey = "process/testProcessInstanceId/task/testTaskDefinitionKey";
    var taskDefinitionKey = "testTaskDefinitionKey";
    var processInstanceId = "testProcessInstanceId";

    var actualKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    assertThat(actualKey).isEqualTo(expectedKey);
  }
}
