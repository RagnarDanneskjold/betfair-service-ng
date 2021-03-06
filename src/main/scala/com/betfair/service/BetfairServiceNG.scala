package com.betfair.service

import akka.actor.ActorSystem
import com.betfair.Configuration
import com.betfair.domain.MarketProjection.MarketProjection
import com.betfair.domain.MarketSort.MarketSort
import com.betfair.domain.OrderProjection.OrderProjection
import com.betfair.domain.MatchProjection.MatchProjection
import com.betfair.domain._
import scala.collection.mutable.HashMap
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

final case class BetfairServiceNGException(message: String) extends Throwable

final class BetfairServiceNG(val config: Configuration, command: BetfairServiceNGCommand)
                            (implicit executionContext: ExecutionContext, system: ActorSystem) {


  def login(): Future[Option[LoginResponse]] = {

    import spray.httpx.PlayJsonSupport._

    val request = LoginRequest(config.username, config.password)
    command.makeLoginRequest(request)
  }

  def logout(sessionToken: String) {

    import spray.httpx.PlayJsonSupport._

    command.makeLogoutRequest(sessionToken)
  }

  def listEventTypes(sessionToken: String, marketFilter: MarketFilter): Future[Option[EventTypeResultContainer]] = {

    import spray.httpx.PlayJsonSupport._

    val params = HashMap[String, Object]("filter" -> marketFilter)
    val request = new JsonrpcRequest(id = "1", method = "SportsAPING/v1.0/listEventTypes", params = params)
    command.makeAPIRequest[EventTypeResultContainer](sessionToken, request)
  }

  def listEvents(sessionToken: String, marketFilter: MarketFilter): Future[Option[EventResultContainer]] = {

    import spray.httpx.PlayJsonSupport._

    val params = HashMap[String, Object]("filter" -> marketFilter)
    val request = new JsonrpcRequest(id = "1", method = "SportsAPING/v1.0/listEvents", params = params)
    command.makeAPIRequest[EventResultContainer](sessionToken, request)
  }

  def listCompetitions(sessionToken: String, marketFilter: MarketFilter): Future[Option[CompetitionResultContainer]] = {

    import spray.httpx.PlayJsonSupport._

    val params = HashMap[String, Object]("filter" -> marketFilter)
    val request = new JsonrpcRequest(id = "1", method = "SportsAPING/v1.0/listCompetitions", params = params)
    command.makeAPIRequest[CompetitionResultContainer](sessionToken, request)
  }

  def listMarketCatalogue(sessionToken: String, marketFilter: MarketFilter, marketProjection: List[MarketProjection], sort: MarketSort,
                           maxResults: Integer): Future[Option[ListMarketCatalogueContainer]] = {

    import spray.httpx.PlayJsonSupport._

    val params = HashMap[String, Object]("filter" -> marketFilter, "marketProjection" -> marketProjection,
      "sort" -> sort, "maxResults" -> maxResults)
    val request = new JsonrpcRequest(id = "1", method = "SportsAPING/v1.0/listMarketCatalogue", params = params)
    command.makeAPIRequest[ListMarketCatalogueContainer](sessionToken, request)
  }

  def listMarketBook(sessionToken: String, marketIds: Set[String],
                     priceProjection: Option[(String,PriceProjection)] = None,
                     orderProjection: Option[(String,OrderProjection)] = None,
                     matchProjection: Option[(String,MatchProjection)] = None,
                     currencyCode: Option[(String,String)] = None): Future[Option[ListMarketBookContainer]] = {

    import spray.httpx.PlayJsonSupport._

    // this simplifies the json serialisation of the Options when in the params HashMap
    val flattenedOpts = Seq(priceProjection, orderProjection, matchProjection, currencyCode).flatten

    val params = HashMap[String, Object]("marketIds" -> marketIds)

    val request = new JsonrpcRequest(id = "1", method = "SportsAPING/v1.0/listMarketBook",
      params = params ++ flattenedOpts.map(i => i._1 -> i._2).toMap)
    command.makeAPIRequest[ListMarketBookContainer](sessionToken, request)
  }

  def placeOrders(sessionToken: String, marketId: String, instructions: Set[PlaceInstruction]): Future[Option[PlaceExecutionReportContainer]] = {

    import spray.httpx.PlayJsonSupport._

    val params = HashMap[String, Object]("marketId" -> marketId, "instructions" -> instructions)

    val request = new JsonrpcRequest(id = "1", method = "SportsAPING/v1.0/placeOrders", params = params)
    command.makeAPIRequest[PlaceExecutionReportContainer](sessionToken, request)
  }

  def cancelOrders(sessionToken: String, marketId: String, instructions: Set[CancelInstruction]): Future[Option[CancelExecutionReportContainer]] = {

    import spray.httpx.PlayJsonSupport._

    val params = HashMap[String, Object]("marketId" -> marketId, "instructions" -> instructions)

    val request = new JsonrpcRequest(id = "1", method = "SportsAPING/v1.0/cancelOrders", params = params)
    command.makeAPIRequest[CancelExecutionReportContainer](sessionToken, request)
  }

  def getExchangeFavourite(sessionToken: String, marketId: String): Future[Option[Runner]] = Future {

    def shortestPrice(runners: Set[Runner]): Runner = {
      if (runners.isEmpty) throw new NoSuchElementException
      runners.reduceLeft((x, y) => if (x.ex.get.availableToBack.head.price < y.ex.get.availableToBack.head.price) x else y)
    }

    val priceProjection = PriceProjection(priceData = Set(PriceData.EX_BEST_OFFERS))
    val favourite = listMarketBook(sessionToken, marketIds = Set(marketId),
      priceProjection = Some(("priceProjection", priceProjection))
    ).map { response =>
      response match {
        case Some(listMarketBookContainer) =>
          Some(shortestPrice(listMarketBookContainer.result(0).runners))
        case error =>
          println("error " + error)
          None
      }
    }
    Await.result(favourite, 10 seconds)
  }

  def getPriceBoundRunners(sessionToken: String, marketId: String, lowerPrice: Double, higherPrice: Double): Future[Option[Set[Runner]]] = Future {

    def filterRunners(runners: Set[Runner]): Set[Runner] = {
      if (runners.isEmpty) throw new NoSuchElementException
      runners.filter(x => (x.ex.get.availableToBack.head.price >= lowerPrice && x.ex.get.availableToBack.head.price <= higherPrice))
    }

    val priceProjection = PriceProjection(priceData = Set(PriceData.EX_BEST_OFFERS))
    val priceBoundRunners = listMarketBook(sessionToken, marketIds = Set(marketId),
      priceProjection = Some(("priceProjection", priceProjection))
    ).map { response =>
      response match {
        case Some(listMarketBookContainer) =>
          Some(filterRunners(listMarketBookContainer.result(0).runners))
        case error =>
          println("error " + error)
          None
      }
    }
    Await.result(priceBoundRunners, 10 seconds)
  }

}