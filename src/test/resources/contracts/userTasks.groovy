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
            },
            {
                "id": "testId2",
                "name": "testTaskName2",
                "assignee": "testAssignee2",
                "created": "2020-12-12T13:03:22.000Z",
                "description": "testDesc2",
                "processDefinitionName": "testProcessDefinitionName2",
                "processInstanceId": "testProcessInstanceId2",
                "processDefinitionId": "testProcessDefinitionId2",
                "formKey": "testFormKey2",
                "suspended":true
            }]
            ''')
    }
}