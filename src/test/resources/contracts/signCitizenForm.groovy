package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should sign form for citizen task by id"

    request {
        urlPath "/api/citizen/task/testId/sign-form"
        method POST()
        headers {
            contentType applicationJson()
            header("x-access-token", "testToken")
        }
        body(
                "data": ["testVar": "testValue"],
                "signature" : "eSign"
        )
    }

    response {
        status NO_CONTENT()
    }
}