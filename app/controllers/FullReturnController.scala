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
import scala.io.Source
import scala.util._
import play.api.libs.json._
import play.api.Logging
import play.api.http.{HeaderNames, Status}

@Singleton()
class FullReturnController @Inject()(cc: ControllerComponents) extends BackendController(cc) with Logging {

  def fullReturn(): Action[AnyContent] = Action.async { implicit request =>
    if (request.headers.get(HeaderNames.AUTHORIZATION).isDefined) {
      val jsonBody: Option[JsValue] = request.body.asJson
      val fullReturnJsonSchema = Try(Source.fromFile("conf/resources/schemas/submit_full_irr.json").mkString) 
      fullReturnJsonSchema match {
        case Success(schema) =>
          val fullJsonSchema : JsValue = Json.parse(schema)
          JsonSchemaHelper.validRequest(fullJsonSchema, jsonBody) match {
              case true => {
                val agentName = (jsonBody.getOrElse(JsString("")) \ "agentDetails" \ "agentName").as[String]
                
                agentName match {
                  case "ServerError" => Future.successful(InternalServerError(agentName))
                  case "ServiceUnavailable" => Future.successful(ServiceUnavailable(agentName))
                  case "Unauthorized" => Future.successful(Unauthorized(agentName))
                  case _ => {
                    val responseString = """{"acknowledgementReference":"1234"}"""
                    val responseJson = Json.parse(responseString)
                    Future.successful(Created(responseJson))
                  }
                }
              }
              case false => Future.successful(BadRequest("test"))
          }
        case Failure(e) => 
          logger.error(s"Error: ${e.getMessage}", e)
          Future.successful(InternalServerError(""))
      }
    } else {
      Future.successful(Unauthorized(""))
    }

  }

}
