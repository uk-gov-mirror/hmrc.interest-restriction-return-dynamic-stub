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

package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import scala.concurrent.Future
import play.api.libs.json.{JsValue, Json}
import play.api.libs.json._
import play.api.Logging
import actions.AuthenticatedAction

@Singleton()
class AbbreviatedReturnController @Inject() (authenticatedAction: AuthenticatedAction, cc: ControllerComponents) extends BackendController(cc) with Logging {

  def abbreviation(): Action[AnyContent] = authenticatedAction.async { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson

    JsonSchemaHelper.applySchemaValidation("/resources/schemas/abbreviated_irr.json", jsonBody) {
      val agentName = jsonBody.flatMap(body => (body \ "agentDetails" \ "agentName").asOpt[String])

      agentName match {
        case Some("ServerError") => Future.successful(InternalServerError(""))
        case Some("ServiceUnavailable") => Future.successful(ServiceUnavailable(""))
        case Some("Unauthorized") => Future.successful(Unauthorized(""))
        case _ => {
          val responseString = """{"acknowledgementReference":"1234"}"""
          val responseJson = Json.parse(responseString)
          Future.successful(Created(responseJson))
        }
      }
    }
  }
}