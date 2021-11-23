/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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