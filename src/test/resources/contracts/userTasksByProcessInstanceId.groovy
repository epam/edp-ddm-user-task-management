package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return tasks by processInstanceId"

    request {
        urlPath("/api/task") {
            queryParameters {
                parameter 'processInstanceId': equalTo("testProcessInstanceId")
            }
        }
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body('''\
            [{
                "id": "testId",
                "name": "testTaskName",
                "assignee": "testAssignee",
                "created": "2020-12-12T13:03:22.000Z",
                "description": "testDesc",
                "processDefinitionName": "testProcessDefinitionName",
                "processInstanceId": "testProcessInstanceId",
                "processDefinitionId": "testProcessDefinitionId",
                "formKey": "testFormKey",
                "suspended": false
            }]
            ''')
    }
}