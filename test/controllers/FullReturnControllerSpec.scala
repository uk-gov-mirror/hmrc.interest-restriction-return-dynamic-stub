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
import play.api.libs.json.{JsValue, Json, JsObject, JsString}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import scala.io.Source

class FullReturnControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val fullReturnJsonSchema = Source.fromFile("conf/resources/schemas/submit_full_irr_apidef.json").mkString
  val fullJsonSchema : JsValue = Json.parse(fullReturnJsonSchema)
  val exampleJsonBody = (fullJsonSchema \ "paths" \ "/organisations/interest-restrictions-return/full" \ "post" \ "requestBody" \ "content" \ "application/json;charset=UTF-8" \ "examples" \ "Full Population" \ "value").as[JsValue]

  val FakeRequestWithHeaders = FakeRequest("POST", "/").withHeaders(HeaderNames.AUTHORIZATION -> "Bearer THhp0fseNReXWL5ljkqrz0bb0wRhgbjT")

  "POST of a full return" should {
    "return 201 when the payload is validated" in {
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleJsonBody);
      val controller = new FullReturnController(Helpers.stubControllerComponents());

      val result = controller.fullReturn()(fakeRequest)
 
      status(result) shouldBe Status.CREATED
    }

    "returns 400 when the payload is invalid" in {
      val exampleInvalidJsonBody = exampleJsonBody.as[JsObject] - "agentDetails"
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleInvalidJsonBody)
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }

    "returns a body containing acknowledgementReference when the payload is validated" in {
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleJsonBody);
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)

      (contentAsJson(result) \ "acknowledgementReference").as[String] shouldBe "1234"
    }

    "returns a 500 when a ServerError agent name is passed" in {
      val amendedBody = changeAgentName(exampleJsonBody, "ServerError")
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(amendedBody)
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

    "returns 503 when a Service unavailable agent name is passed" in {
      val amendedBody = changeAgentName(exampleJsonBody, "ServiceUnavailable")
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(amendedBody)
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.SERVICE_UNAVAILABLE
    }

    "returns 401 when an Unauthorized agent name is passed" in {
      val amendedBody = changeAgentName(exampleJsonBody, "Unauthorized")
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(amendedBody)
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "returns 201 when a bearer token is passed" in {
      val fakeRequest = FakeRequestWithHeaders.withJsonBody(exampleJsonBody)
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.CREATED
    }

    "returns 401 when a bearer token is not passed" in {
      val fakeRequest = FakeRequest("POST", "/").withJsonBody(exampleJsonBody)
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

  }

  def changeAgentName(body: JsValue, newAgentName: String): JsObject = {
    val agentDetails = body.as[JsObject] \ "agentDetails"
    val amendedAgentDetails = agentDetails.as[JsObject] + ("agentName" -> JsString(newAgentName))
    exampleJsonBody.as[JsObject] + ("agentDetails" -> amendedAgentDetails)
  }
}

/*
MDTP will need following Kong Bearer token to access these EIS APIS and based on my past conversation with MDTP (for some other project) 
they need a day to configure this bearer token at their end.
Kong token : THhp0fseNReXWL5ljkqrz0bb0wRhgbjT*/