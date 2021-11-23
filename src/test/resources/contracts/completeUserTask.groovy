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