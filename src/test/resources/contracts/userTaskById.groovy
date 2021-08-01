package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user-task by id"

    request {
        urlPath "/api/task/testId"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                id: "testId",
                name: "testTaskName",
                assignee: "testAssignee",
                created: "2020-12-12T13:03:22.000Z",
                description: "testDesc",
                processInstanceId: "testProcessInstanceId",
                processDefinitionId: "testProcessDefinitionId",
                formKey: "testFormKey",
                suspended: true,
                esign: true,
                data: [var1: 123123],
                formVariables: [fullName: "FullName"]
        )

    }
}