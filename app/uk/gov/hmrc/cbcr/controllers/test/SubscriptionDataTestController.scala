/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.cbcr.controllers.test

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.json.JsValue

import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.cbcr.models.SubscriptionDetails
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDataTestController @Inject()(private val mongo: ReactiveMongoApi)(implicit ec:ExecutionContext) extends BaseController with ServicesConfig {

  val repository: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection]("Subscription_Data"))

  def save(s: SubscriptionDetails): Future[WriteResult] =
    repository.flatMap(_.insert(s))


  def insertData() = Action.async[JsValue](parse.json) {
    implicit request =>
      withJsonBody[SubscriptionDetails] {
        save(_) map {
          _.ok match {
            case true => Ok("data submitted successfully")
            case false =>
              Logger.warn(request.body.toString)
              NotFound("error submitting data")
          }

        }


      }

  }
}
