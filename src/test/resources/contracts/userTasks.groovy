/*
 * Copyright 2021 EPAM Systems.
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

package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user tasks"

    request {
        urlPath "/api/task"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [
                id                   : "testId",
                name                 : "testTaskName",
                assignee             : "testAssignee",
                created              : "2020-12-12T13:03:22.000Z",
                description          : "testDesc",
                processDefinitionName: "testProcessDefinitionName",
                processInstanceId    : "testProcessInstanceId",
                processDefinitionId  : "testProcessDefinitionId",
                formKey              : "testFormKey",
                suspended            : false,
                businessKey          : "businessKey"
            ],
            [
                id                   : "testId2",
                name                 : "testTaskName2",
                assignee             : "testAssignee2",
                created              : "2020-12-12T13:03:22.000Z",
                description          : "testDesc2",
                processDefinitionName: "testProcessDefinitionName2",
                processInstanceId    : "testProcessInstanceId2",
                processDefinitionId  : "testProcessDefinitionId2",
                formKey              : "testFormKey2",
                suspended            : true,
                businessKey          : null
            ]
        ])
    }
}