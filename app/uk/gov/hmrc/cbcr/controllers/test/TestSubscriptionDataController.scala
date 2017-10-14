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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.cbcr.models.{DocRefId, DocRefIdRecord, SubscriptionDetails, Utr}
import uk.gov.hmrc.cbcr.repositories.{DocRefIdRepository, SubscriptionDataRepository}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestSubscriptionDataController @Inject()(subRepo: SubscriptionDataRepository, docRefRepo: DocRefIdRepository , private val mongo: ReactiveMongoApi)(implicit ec: ExecutionContext) extends BaseController with ServicesConfig {

//  val repository: Future[JSONCollection] =
//    mongo.database.map(_.collection[JSONCollection]("Subscription_Data"))

//  val docRefIdRepository: Future[JSONCollection] =
//    mongo.database.map(_.collection[JSONCollection]("DocRefId"))

  def insertData() = Action.async[JsValue](parse.json) {
    implicit request =>
      withJsonBody[SubscriptionDetails] {
        subRepo.save(_) map {
          _.ok match {
            case true => Ok("data submitted successfully")
            case false =>
              Logger.warn(request.body.toString)
              NotFound("error submitting data")
          }
        }
      }
  }

  def deleteSubscription(utrs: String): Action[AnyContent] = Action.async{
    implicit request =>
    {
      val utr = Utr(utrs)
      subRepo.clear(utr).map{w =>
        Logger.error(s"Response : $w")
        if(w.n > 0) Ok  else NotFound
      }
    }
  }

//  def save(s: SubscriptionDetails): Future[WriteResult] =
//    repository.flatMap(_.insert(s))


//  def clearSubscription(utr:Utr): Future[WriteResult] = {
//    val criteria = Json.obj("utr" -> utr.utr)
//    for {
//      repo   <- repository
//      _      <- repo.find(criteria).one[SubscriptionDetails]
//      result <- repo.remove(criteria, firstMatchOnly = true)
//    } yield result
//  }
//
//  def deleteSubscription(utrs: String): Action[AnyContent] = Action.async{
//    implicit request =>
//      {
//        val utr = Utr(utrs)
//        clearSubscription(utr).map{w =>
//          Logger.error(s"Response : $w")
//          if(w.n > 0) Ok  else NotFound
//        }
//      }
//  }

//  def clearSingleDocRefId(docRefId: DocRefId): Future[WriteResult] = {
//    val criteria = Json.obj("id" -> docRefId.id)
//    for {
//      repo <- docRefIdRepository
//      _    <- repo.find(criteria).one[DocRefIdRecord]
//      result <- repo.remove(criteria, firstMatchOnly = true)
//    } yield result
//  }

  def deleteSingleDocRefId(docRefIds: String): Action[AnyContent] = Action. async{
    implicit request =>
      {
        val docRefId = DocRefId(docRefIds)
        docRefRepo.delete(docRefId).map{w =>
          Logger.error(s"Response : $w")
          if(w.n > 0) Ok else NotFound
        }
      }
  }

}
