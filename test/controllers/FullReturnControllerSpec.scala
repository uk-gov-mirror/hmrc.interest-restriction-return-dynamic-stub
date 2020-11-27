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
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import scala.io.Source

class FullReturnControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  val fullReturnJsonSchema = Source.fromFile("conf/resources/schemas/submit_full_irr.json").mkString
  val fullJsonSchema : JsValue = Json.parse(fullReturnJsonSchema)
  val exampleJsonBody = (fullJsonSchema \ "paths" \ "/organisations/interest-restrictions-return/full" \ "post" \ "requestBody" \ "content" \ "application/json;charset=UTF-8" \ "examples" \ "Full Population" \ "value").as[JsValue]

  "POST of a full return" should {
    "return 201 when the payload is validated" in {
      val fakeRequest = FakeRequest("POST", "/").withJsonBody(exampleJsonBody);
      val controller = new FullReturnController(Helpers.stubControllerComponents());

      val result = controller.fullReturn()(fakeRequest)

      status(result) shouldBe Status.CREATED
    }

    "returns 400 when the payload is invalid" in {
      val fakeRequest = FakeRequest("POST", "/").withJsonBody(Json.obj("test" -> "test"))
      val controller = new FullReturnController(Helpers.stubControllerComponents())

      val result = controller.fullReturn()(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
    }
  }
}
