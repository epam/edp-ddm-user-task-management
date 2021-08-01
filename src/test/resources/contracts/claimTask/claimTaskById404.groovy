package contracts.claimTask

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "claimTaskById404"
    description "should throw 404 if user-task not found"

    request {
        urlPath "/api/task/testId404/claim"
        method POST()
        headers {
            header("x-access-token", "testToken")
        }
    }

    response {
        status NOT_FOUND()
        body(
                traceId: 'traceId',
                code: 'code404',
                message: '404 message',
                localizedMessage: '404 localizedMessage'
        )
    }
}