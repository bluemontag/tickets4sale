package model

import zio.json.internal.{RetractReader, Write}
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder, JsonError}

sealed trait TicketSaleStatus

case object SaleNotStarted extends TicketSaleStatus {
  override def toString: String = "Sale Not Started"
}

case object OpenForSale extends TicketSaleStatus {
  override def toString: String = "Open For Sale"
}

case object SoldOut extends TicketSaleStatus {
  override def toString: String = "Sold Out"
}

case object InThePast extends TicketSaleStatus {
  override def toString: String = "In The Past"
}

object TicketSaleStatus {

  implicit val decoder: JsonDecoder[TicketSaleStatus] = new JsonDecoder[TicketSaleStatus] {
    override def unsafeDecode(trace: List[JsonError], in: RetractReader): TicketSaleStatus = ???
  }

  implicit val encoder: JsonEncoder[TicketSaleStatus] = new JsonEncoder[TicketSaleStatus] {
    override def unsafeEncode(a: TicketSaleStatus, indent: Option[Int], out: Write): Unit = out.write(" \"" + a.toString() + "\" ")
  }

}