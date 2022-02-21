package com.epam.digital.data.platform.usrtaskmgt.remote.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.bpms.api.dto.DdmTaskQueryDto;
import com.epam.digital.data.platform.bpms.client.TaskRestClient;
import com.epam.digital.data.platform.usrtaskmgt.mapper.UserTaskDtoMapper;
import com.epam.digital.data.platform.usrtaskmgt.model.request.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserTaskRemoteServiceImplTest {

  @Mock
  private TaskRestClient client;
  @Spy
  private UserTaskDtoMapper userTaskDtoMapper = Mappers.getMapper(UserTaskDtoMapper.class);
  @InjectMocks
  private UserTaskRemoteServiceImpl service;

  @Test
  void shouldSetCorrectProcessInstanceIdGetUserTasks() {
    var processInstanceId = "id";

    service.getUserTasks(processInstanceId, "assignee", new Pageable());

    var captor = ArgumentCaptor.forClass(DdmTaskQueryDto.class);
    verify(client).getTasksByParams(captor.capture(), any());
    var ddmTaskQueryDto = captor.getValue();
    assertThat(ddmTaskQueryDto.getProcessInstanceId()).isEqualTo(processInstanceId);
  }

  @Test
  void shouldSetCorrectProcessInstanceIdGetLightweightUserTasks() {
    var rootProcessInstanceId = "id";

    service.getLightweightUserTasks(rootProcessInstanceId, "assignee", new Pageable());

    var captor = ArgumentCaptor.forClass(DdmTaskQueryDto.class);
    verify(client).getLightweightTasksByParams(captor.capture(), any());
    var ddmTaskQueryDto = captor.getValue();
    assertThat(ddmTaskQueryDto.getRootProcessInstanceId()).isEqualTo(rootProcessInstanceId);
  }
}