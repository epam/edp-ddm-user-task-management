package com.epam.digital.data.platform.usrtaskmgt.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class CephKeyProviderTest {

  private CephKeyProvider cephKeyProvider;

  @Before
  public void init() {
    cephKeyProvider = new CephKeyProvider();
  }

  @Test
  public void testGeneratingCephKey() {
    var expectedKey = "process/testProcessInstanceId/task/testTaskDefinitionKey";
    var taskDefinitionKey = "testTaskDefinitionKey";
    var processInstanceId = "testProcessInstanceId";

    var actualKey = cephKeyProvider.generateKey(taskDefinitionKey, processInstanceId);

    assertThat(actualKey).isEqualTo(expectedKey);
  }
}
