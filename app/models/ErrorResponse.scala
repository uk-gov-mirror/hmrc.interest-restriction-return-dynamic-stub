/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import play.api.libs.json._

case class ErrorResponse(failures: List[FailureMessage])

case class FailureMessage(code: String, reason: String)

object ErrorResponse {
  implicit val reads: OFormat[ErrorResponse] = Json.format[ErrorResponse]
}

object FailureMessage {
  implicit val reads: OFormat[FailureMessage] = Json.format[FailureMessage]

  val InvalidJson = FailureMessage("InvalidJson", "The JSON provided was not valid.")
  val ServerError = FailureMessage("ServerError", "An Internal Server Error occured.")
  val ServiceUnavailable = FailureMessage("ServiceUnavailable", "Service is not available.")
  val Unauthorized = FailureMessage("Unauthorized","Request Unauthorized.")
  val MissingBearerToken = FailureMessage("MissingBearerToken","Bearer token is missing.")
  val MissingBody = FailureMessage("MissingBody","There was no body provided.")
}
