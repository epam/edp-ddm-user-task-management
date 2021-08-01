package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return completed user tasks"

    request {
        urlPath "/api/history/task"
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
                "startTime": "2020-12-12T13:03:22.000Z",
                "endTime": "2020-12-12T13:03:22.000Z",
                "description": "testDesc",
                "processDefinitionName": "testProcessDefinitionName",
                "processInstanceId": "testProcessInstanceId",
                "processDefinitionId": "testProcessDefinitionId"
            },
            {
                "id": "testId2",
                "name": "testTaskName2",
                "assignee": "testAssignee2",
                "startTime": "2020-12-12T13:03:22.000Z",
                "endTime": "2020-12-12T13:03:22.000Z",
                "description": "testDesc2",
                "processDefinitionName": "testProcessDefinitionName2",
                "processInstanceId": "testProcessInstanceId2",
                "processDefinitionId": "testProcessDefinitionId2"
            }]
            ''')
    }
}