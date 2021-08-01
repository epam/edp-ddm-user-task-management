package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return user-task count"

    request {
        urlPath "/api/task/count"
        method GET()
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                count: 22
        )
    }
}