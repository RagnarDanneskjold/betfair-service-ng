package com.betfair.domain

import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

case class MarketBook(marketId: String,
                      isMarketDataDelayed: Boolean,
                      status: String,
                      betDelay: Int,
                      bspReconciled: Boolean,
                      complete: Boolean,
                      inplay: Boolean,
                      numberOfWinners: Int,
                      numberOfRunners: Int,
                      numberOfActiveRunners: Int,
                      lastMatchTime: Option[DateTime],
                      totalMatched: Double,
                      totalAvailable: Double,
                      crossMatching: Boolean,
                      runnersVoidable: Boolean,
                      version: Long, runners: List[Runner])

object MarketBook {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  implicit val dateTimeReads = Reads.jodaDateReads(dateFormat)
  implicit val formatMarketBook = Json.format[MarketBook]
}