package contracts.claimTask

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "claimTaskById"
    description "should claim user-task by id"

    request {
        urlPath "/api/task/testId/claim"
        method POST()
        headers {
            header("x-access-token", "testToken")
        }
    }

    response {
        status NO_CONTENT()
    }
}