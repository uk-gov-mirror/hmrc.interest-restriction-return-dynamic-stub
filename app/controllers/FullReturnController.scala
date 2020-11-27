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
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import play.api.libs.json.{JsValue, Json}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.core.JsonParser
import scala.io.Source
import scala.util._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logging

@Singleton()
class FullReturnController @Inject()(cc: ControllerComponents) extends BackendController(cc) with Logging {

  private final lazy val jsonMapper = new ObjectMapper()
  private final lazy val jsonFactory = jsonMapper.getFactory

  def loadRequestSchema(requestSchema: JsValue): JsonSchema = {
    val schemaMapper = new ObjectMapper()
    val factory = schemaMapper.getFactory
    val schemaParser: JsonParser = factory.createParser(requestSchema.toString)
    val schemaJson: JsonNode = schemaMapper.readTree(schemaParser)
    JsonSchemaFactory.byDefault().getJsonSchema(schemaJson)
  }

  def validRequest(jsonSchema: JsValue, json: Option[JsValue]): Boolean = {
    json.fold(true) { response =>
      val jsonParser = jsonFactory.createParser(response.toString)
      val jsonNode: JsonNode = jsonMapper.readTree(jsonParser)
      val result: ProcessingReport = loadRequestSchema(jsonSchema).validate(jsonNode)
      logger.error(s"Outputs: ${result}")
      result.isSuccess
    }
  }
  //    val source = Source.fromFile(getClass.getResource(s"/$filename").getPath)

  /*
    private[utils] def loadRequestSchema(schemaName: String, version: String): JsonSchema = {
    val file = new File(s"public/api/conf/$version/schemas/$schemaName")
    val uri = file.toURI
    JsonSchemaFactory.byDefault().getJsonSchema(uri.toString)
  }*/

  def fullReturn(): Action[AnyContent] = Action.async { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson
    //    val source = Source.fromFile(getClass.getResource(s"/$filename").getPath)
    ///   // conf/resources/submit_full_irr.json
    // val fullReturnJsonSchema = Try(Source.fromResource("/resources/submit_full_irr.json").getLines.mkString)
    val fullReturnJsonSchema = Try(Source.fromFile("conf/resources/schemas/submit_full_irr.json").mkString)
    fullReturnJsonSchema match {
      case Success(schema) =>
        val fullJsonSchema : JsValue = Json.parse(schema)
        val jsonSchema = (fullJsonSchema \ "components" \ "schemas" \ "request").as[JsValue]
        validRequest(jsonSchema, jsonBody) match {
            case true => Future.successful(Created("test"))
            case false => Future.successful(BadRequest("test"))
        }
      case Failure(e) => 
        logger.error(s"Error: ${e.getMessage}", e)
        Future.successful(InternalServerError(""))
    }

  }
}
