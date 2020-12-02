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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._

import scala.io.Source
import actions.AuthenticatedAction
import play.api.mvc.BodyParsers

class FullReturnControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val fullReturnJsonSchema = Source.fromFile("conf/resources/api/submit_full_irr_apidef.json").mkString
  val fullJsonSchema : JsValue = Json.parse(fullReturnJsonSchema)
  val exampleJsonBody = (fullJsonSchema \ "paths" \ "/organisations/interest-restrictions-return/full" \ "post" \ "requestBody" \ "content" \ "application/json;charset=UTF-8" \ "examples" \ "Full Population" \ "value").as[JsValue]

  val FakeRequestWithHeaders = FakeRequest("POST", "/").withHeaders(HeaderNames.AUTHORIZATION -> "Bearer THhp0fseNReXWL5ljkqrz0bb0wRhgbjT")

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]
  val authenticatedAction: AuthenticatedAction = new AuthenticatedAction(bodyParsers)

  "POST of a full return" should {
    "return 201 when the payload is validated" in {
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleJsonBody);
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents());

      val result = controller.fullReturn()(fakeRequest)
 
      status(result) shouldBe Status.CREATED
    }

    "returns 400 when the payload is invalid" in {
      val exampleInvalidJsonBody = exampleJsonBody.as[JsObject] - "agentDetails"
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleInvalidJsonBody)
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }

    "returns a body containing acknowledgementReference when the payload is validated" in {
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleJsonBody);
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)

      (contentAsJson(result) \ "acknowledgementReference").as[String] shouldBe "1234"
    }

    "returns a 500 when a ServerError agent name is passed" in {
      val amendedBody = changeAgentName(exampleJsonBody, Some("ServerError"))
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(amendedBody)
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "returns 503 when a Service unavailable agent name is passed" in {
      val amendedBody = changeAgentName(exampleJsonBody, Some("ServiceUnavailable"))
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(amendedBody)
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.SERVICE_UNAVAILABLE
    }

    "returns 401 when an Unauthorized agent name is passed" in {
      val amendedBody = changeAgentName(exampleJsonBody, Some("Unauthorized"))
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(amendedBody)
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "returns 201 when a bearer token is passed" in {
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleJsonBody)
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "returns 401 when a bearer token is not passed" in {
      val fakeRequest = FakeRequest("POST", "/").withJsonBody(exampleJsonBody)
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
      contentAsString(result) shouldBe "Missing Bearer Token"
    }
    
    "returns 400 when a body is empty" in {
      val fakeRequest = FakeRequestWithHeaders
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "Missing body"
    }

    "returns 201 when no agent name is passed" in {
      val amendedBody = changeAgentName(exampleJsonBody, None)
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(amendedBody)
      val controller = new FullReturnController(authenticatedAction, Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }g
  }

  def changeAgentName(body: JsValue, newAgentName: Option[String]): JsObject = {
    val agentDetails = body.as[JsObject] \ "agentDetails"
    val amendedAgentDetails = newAgentName match {
      case Some(name) => agentDetails.as[JsObject] + ("agentName" -> JsString(name))
      case None => agentDetails.as[JsObject] - "agentName"
    }
    body.as[JsObject] + ("agentDetails" -> amendedAgentDetails)
  }
}