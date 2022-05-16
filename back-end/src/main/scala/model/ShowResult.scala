package model

import config.TicketsConfig
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class ShowResult(title: String,
                      opening: String,
                      ticketsLeft: Int,
                      ticketsAvailable: Int,
                      status: TicketSaleStatus,
                      price: TicketsConfig.Price)

object ShowResult {
  implicit val decoder: JsonDecoder[ShowResult] =
    DeriveJsonDecoder.gen[ShowResult]
  implicit val encoder: JsonEncoder[ShowResult] =
    DeriveJsonEncoder.gen[ShowResult]
}