package contracts.claimTask

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "claimTaskById409"
    description "should throw 409 if user-task already assigned"

    request {
        urlPath "/api/task/testId409/claim"
        method POST()
        headers {
            header("x-access-token", "testToken")
        }
    }

    response {
        status CONFLICT()
        body(
                traceId: 'traceId',
                code: '409 CONFLICT',
                message: '409 message',
                localizedMessage: '409 localizedMessage'
        )
    }
}