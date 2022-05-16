package config

import model.GenreObj._
import model._

import java.time.format.DateTimeFormatter
import scala.util.matching.Regex

object TicketsConfig {

  val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")


  val SHOW_CSV_FILE_COLUMN_PATTERN: Regex = "(\"?[a-zA-Z0-9 ,'’`*&$@!/\\(\\),-:]+\"?),(\\d{4}-\\d{2}-\\d{2}),(COMEDY|DRAMA|MUSICAL)".r

  // capacity
  val BIG_HALL_CAPACITY = 200
  val SMALL_HALL_CAPACITY = 100

  // tickets available for sale per day
  val BIG_HALL_TICKETS_PER_DAY = 10
  val SMALL_HALL_TICKETS_PER_DAY = 5

  // day limits
  val MAX_DAYS_IN_BIG_HALL = 60
  val MAX_DAYS_IN_THEATER = 100
  val SALE_STARTING_DAY_DIFF = -25
  val SALE_ENDING_DAY_DIFF = -5

  // discounts & prices
  type Price = Double
  val DAY_N_FOR_DISCOUNT = 80
  val DISCOUNT_PERCENTAGE_FACTOR: Double = 0.80
  val ticketPriceByGenre: Map[GenreObj.Genre, Price] = Map(MUSICAL -> 70.0, COMEDY -> 50.0, DRAMA -> 40.0)
}
