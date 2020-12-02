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

import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import play.api.libs.json.{JsValue, Json}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.core.JsonParser
import scala.concurrent.Future
import play.api.Logging
import play.api.mvc._
import play.api.mvc.Results._
import scala.util.{Try, Success, Failure}
import scala.io.Source
import play.api.libs.json._

object JsonSchemaHelper extends Logging {

  private final lazy val jsonMapper = new ObjectMapper()
  private final lazy val jsonFactory = jsonMapper.getFactory

  def loadRequestSchema(requestSchema: JsValue): JsonSchema = {
    val schemaMapper = new ObjectMapper()
    val factory = schemaMapper.getFactory
    val schemaParser: JsonParser = factory.createParser(requestSchema.toString)
    val schemaJson: JsonNode = schemaMapper.readTree(schemaParser)
    JsonSchemaFactory.byDefault().getJsonSchema(schemaJson)
  }

  def validRequest(jsonSchema: JsValue, json: Option[JsValue]): Option[ProcessingReport] = {
    json.map { response =>
      val jsonParser = jsonFactory.createParser(response.toString)
      val jsonNode: JsonNode = jsonMapper.readTree(jsonParser)
      val result: ProcessingReport = loadRequestSchema(jsonSchema).validate(jsonNode)
      result
    }
  }

  def applySchemaValidation(schemaPath: String)(f: => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    val schemaFile = Source.fromFile(schemaPath)
    val fullReturnJsonSchema = Try(schemaFile.mkString)
    val jsonBody: Option[JsValue] = request.body.asJson
    val result = fullReturnJsonSchema match {
      case Success(schema) =>
        val fullJsonSchema : JsValue = Json.parse(schema)
        val validationResult = JsonSchemaHelper.validRequest(fullJsonSchema, jsonBody)
        validationResult match {
            case Some(res) if res.isSuccess => f
            case Some(res) => Future.successful(BadRequest(res.toString()))
            case _ => Future.successful(BadRequest("Missing body"))
        }
      case Failure(e) =>
        logger.error(s"Error: ${e.getMessage}", e)
        Future.successful(InternalServerError(""))
    }
    schemaFile.close()
    result
  }


}

