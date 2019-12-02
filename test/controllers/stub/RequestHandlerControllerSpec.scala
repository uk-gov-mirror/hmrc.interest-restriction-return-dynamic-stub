/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.stub

import mocks.{MockDataRepository, MockSchemaValidation}
import models.HttpMethod._
import models.{DataIdModel, DataModel}
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}
import play.mvc.Http.Status
import utils.TestSupport

class RequestHandlerControllerSpec extends TestSupport with MockSchemaValidation with MockDataRepository {

  object TestRequestHandlerController extends RequestHandlerController(mockSchemaValidation, mockDataRepository, Helpers.stubControllerComponents())

  lazy val successWithBodyModel: String => DataModel = method => DataModel(
    _id = DataIdModel("test", method),
    schemaId = "testID2",
    status = Status.OK,
    response = Some(Json.parse("""{"something" : "hello"}"""))
  )

  "The getRequestHandler method" should {

    lazy val dataModel = successWithBodyModel(GET)

    "return the status and body" in {
      lazy val result = TestRequestHandlerController.getRequestHandler(dataModel._id.url)(FakeRequest())

      mockFindById(dataModel._id)(Some(dataModel))
      status(result) shouldBe Status.OK
      await(bodyOf(result)) shouldBe s"${dataModel.response.get}"
    }

    "return a 404 status when the endpoint cannot be found" in {
      lazy val result = TestRequestHandlerController.getRequestHandler(dataModel._id.url)(FakeRequest())

      mockFindById(dataModel._id)()
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "The postRequestHandler method" should {

    lazy val dataModel = successWithBodyModel(POST)
    lazy val request = FakeRequest().withJsonBody(Json.obj("foo" -> "bar"))

    "return the corresponding response of an incoming POST request" in {

      lazy val result = TestRequestHandlerController.postRequestHandler(dataModel._id.url)(request)

      mockFindById(DataIdModel(dataModel._id.url, POST))(Some(dataModel))
      mockValidateRequestJson(dataModel.schemaId, request.body.asJson)(success = true)

      await(bodyOf(result)) shouldBe s"${dataModel.response.get}"
    }

    "return a 404 response status when there is no stubbed response body for an incoming POST request" in {
      lazy val result = TestRequestHandlerController.postRequestHandler(dataModel._id.url)(request)

      mockFindById(dataModel._id)()
      status(result) shouldBe Status.NOT_FOUND
    }
  }


  "The putRequestHandler method" should {

    lazy val dataModel = successWithBodyModel(PUT)
    lazy val request = FakeRequest().withJsonBody(Json.obj("foo" -> "bar"))

    "return the corresponding response of an incoming PUT request" in {
      lazy val result = TestRequestHandlerController.putRequestHandler(dataModel._id.url)(request)

      mockFindById(dataModel._id)(Some(dataModel))
      mockValidateRequestJson(dataModel.schemaId, request.body.asJson)(success = true)

      await(bodyOf(result)) shouldBe s"${dataModel.response.get}"
    }

    "return a 404 response status when there is no stubbed response body for an incoming PUT request" in {
      lazy val result = TestRequestHandlerController.putRequestHandler(dataModel._id.url)(request)

      mockFindById(dataModel._id)()
      status(result) shouldBe Status.NOT_FOUND
    }
  }

}
