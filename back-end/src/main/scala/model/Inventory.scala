package model

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.time.LocalDate

case class Inventory(inventory: List[InventoryItem])

case class InventoryItem(genre: String, shows: List[ShowResult])

object InventoryItem {
  implicit val decoder: JsonDecoder[InventoryItem] =
    DeriveJsonDecoder.gen[InventoryItem]
  implicit val encoder: JsonEncoder[InventoryItem] =
    DeriveJsonEncoder.gen[InventoryItem]
}

object Inventory {
  implicit val decoder: JsonDecoder[Inventory] =
    DeriveJsonDecoder.gen[Inventory]
  implicit val encoder: JsonEncoder[Inventory] =
    DeriveJsonEncoder.gen[Inventory]

  def apply(shows: List[Show], queryDate: LocalDate, showDate: LocalDate): Inventory = {
    val grouped = shows.groupBy( res => res.genre.toString)

    val processed: List[InventoryItem] = grouped.map { resPair =>
      val showResults = resPair._2.map(_.toShowResult(queryDate, showDate))
      val showsFiltered = showResults.filter( res => res.ticketsLeft > 0)
      InventoryItem(resPair._1, showsFiltered)
    }.toList

    val genresFiltered = processed.filter( item => item.shows.size > 0)

    Inventory(genresFiltered)
  }
}
