package contracts.claimTask

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "claimTaskById500"
    description "should throw 500 in case of internal server error"

    request {
        urlPath "/api/task/testId500/claim"
        method POST()
        headers {
            header("x-access-token", "testToken")
        }
    }

    response {
        status INTERNAL_SERVER_ERROR()
        body(
                traceId: 'traceId',
                code: 'code500',
                message: '500 message',
                localizedMessage: '500 localizedMessage'
        )
    }
}