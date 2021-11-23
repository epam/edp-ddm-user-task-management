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