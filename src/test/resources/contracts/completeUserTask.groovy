package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should complete user-task by id"

    request {
        urlPath "/api/task/testId/complete"
        method POST()
        headers {
            contentType applicationJson()
            header("x-access-token", "testToken")
        }
        body(
                "data":
                        ["testVar": ["value": "testValue"]]
        )
    }

    response {
        status NO_CONTENT()
    }
}