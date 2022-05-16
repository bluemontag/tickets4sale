package model

import config.TicketsConfig
import model.GenreObj.{COMEDY, DRAMA, Genre, MUSICAL}

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

case class Show(title: String, opening: LocalDate, genre: Genre, private val seatsAvailable: Map[LocalDate, Int]) {

  def daysFromOpening(localDate: LocalDate): Long = DAYS.between(opening, localDate)
  def showDayNumber(showDate: LocalDate): Long = DAYS.between(opening, showDate ) + 1    // first day is day 1

  def priceForDay(showDate: LocalDate): TicketsConfig.Price = {
    val dayNumber = this.showDayNumber(showDate)
    val regularPrice = TicketsConfig.ticketPriceByGenre(this.genre)
    val isInTheater = this.isInTheater(showDate)
    val discountDay = TicketsConfig.DAY_N_FOR_DISCOUNT

    dayNumber match {
      case _ if !isInTheater => 0 // edge case
      case n if n <= discountDay => regularPrice
      case n if n > discountDay => regularPrice * TicketsConfig.DISCOUNT_PERCENTAGE_FACTOR
    }
  }

  /**
   * queryDayNumber >= saleStartingDay && queryDayNumber <= saleEndingDay
   */
  def saleIsOn(queryDate: LocalDate, showDate: LocalDate): Boolean = {
    val saleStartingDate = Show.getFirstDayOfSalesInclusive(showDate)
    val saleEndingDate = Show.getLastDayOfSalesInclusive(showDate)

    (queryDate.isEqual(saleStartingDate) || queryDate.isAfter(saleStartingDate)) &&
    (queryDate.isEqual(saleEndingDate) || queryDate.isBefore(saleEndingDate))
  }

  /**
   * queryDayNumber < saleStartingDay
   */
  def saleHasNotStartedYet(queryDate: LocalDate, showDate: LocalDate): Boolean = {
    val saleStartingDate = Show.getFirstDayOfSalesInclusive(showDate)

    queryDate.isBefore(saleStartingDate)
  }

  def saleHasPassed(queryDate: LocalDate, showDate: LocalDate): Boolean = {
    !saleIsOn(queryDate, showDate) && !saleHasNotStartedYet(queryDate, showDate)
  }

  def getSaleStatus(queryDate: LocalDate, showDate: LocalDate): TicketSaleStatus = {
    val saleIsOn = this.saleIsOn(queryDate, showDate)
    if (saleIsOn && this.ticketsAvailableForSale(queryDate, showDate) > 0) {
      OpenForSale
    } else if (saleIsOn) {
      SoldOut
    } else if (saleHasNotStartedYet(queryDate, showDate)) {
      SaleNotStarted
    } else {
      InThePast
    }
  }

  def isInSmallHall(showDate: LocalDate): Boolean = {
    val showDayNumber: Long = this.showDayNumber(showDate)
    val isInTheater = this.isInTheater(showDate)

    isInTheater && showDayNumber > TicketsConfig.MAX_DAYS_IN_BIG_HALL
  }

  def isInTheater(showDate: LocalDate) = {
    val showDayNumber = this.showDayNumber(showDate)
    showDayNumber >= 1 && showDayNumber <= TicketsConfig.MAX_DAYS_IN_THEATER
  }

  def isInBigHall(showDate: LocalDate): Boolean = {
    val showDayNumber = this.showDayNumber(showDate)
    val isInTheater = this.isInTheater(showDate)

    isInTheater && showDayNumber <= TicketsConfig.MAX_DAYS_IN_BIG_HALL
  }

  def ticketsAvailable(queryDate: LocalDate, showDate: LocalDate): Int = {
    val (totalTicketsForHall, ticketsSoldPerDayForHall): (Option[Int], Int) = if (this.isInBigHall(showDate))
      (this.seatsAvailable.get(showDate), TicketsConfig.BIG_HALL_TICKETS_PER_DAY)
    else
      (this.seatsAvailable.get(showDate), TicketsConfig.SMALL_HALL_TICKETS_PER_DAY)

    totalTicketsForHall match {
      case None => 0
      case Some(total) =>
        if (this.saleHasNotStartedYet(queryDate, showDate))
          total // all the tickets are available since the sale does not started yet
        else if (this.saleIsOn(queryDate, showDate)) {
          val elapsed = this.getElapsedDaysFactor(queryDate, showDate)
          total - (elapsed * ticketsSoldPerDayForHall)
        } else
          0 // if the sale has finished, the available tickets is 0
    }
  }

  def getElapsedDaysFactor(queryDate: LocalDate, showDate: LocalDate): Int = {
    val saleStartingDate = Show.getFirstDayOfSalesInclusive(showDate)
    val salesElapsedDays = DAYS.between(saleStartingDate, queryDate )
    if (salesElapsedDays <= 0)
      0
    else
      salesElapsedDays.toInt
  }

  def ticketsAvailableForSale(queryDate: LocalDate, showDate: LocalDate): Int = {
    val (totalTicketsForHall, ticketsSoldPerDayForHall): (Option[Int], Int) = if (this.isInBigHall(showDate))
      (this.seatsAvailable.get(showDate), TicketsConfig.BIG_HALL_TICKETS_PER_DAY)
    else
      (this.seatsAvailable.get(showDate), TicketsConfig.SMALL_HALL_TICKETS_PER_DAY)

    totalTicketsForHall match {
      case None => 0
      case Some(total) =>
        if (this.saleIsOn(queryDate, showDate))
          math.min(ticketsSoldPerDayForHall, total)
        else
          0 // if the sale is not ongoing, the are no available tickets
    }
  }

  override def toString() = {
    "Show(title=" + title + ", opening=" + opening.toString + ", genre=" + genre.toString + ")"
  }

  def toShowResult(queryDate: LocalDate, showDate: LocalDate): ShowResult = {
    ShowResult(title = title,
               opening = this.opening.toString,
               ticketsLeft = this.ticketsAvailable(queryDate, showDate),
               ticketsAvailable = this.ticketsAvailableForSale(queryDate, showDate),
               status = getSaleStatus(queryDate, showDate),
               price = this.priceForDay(showDate)
              )
  }
}

object Show {
  def apply(title: String, openingDate: String, genreStr: String): Show = {

    val opening = LocalDate.parse(openingDate, TicketsConfig.DATE_FORMATTER)

    val genreEnum = genreStr.toUpperCase() match {
      case "COMEDY" => COMEDY
      case "DRAMA" => DRAMA
      case "MUSICAL" => MUSICAL
    }

    val seats = this.generateMapAvailabilityForDate(opening)

    Show(title, opening, genreEnum, seats)
  }

  private def generateMapAvailabilityForDate(openingDate: LocalDate): Map[LocalDate, Int] = {
    assume(TicketsConfig.MAX_DAYS_IN_THEATER >= 2)
    assume(TicketsConfig.MAX_DAYS_IN_BIG_HALL >= 1)
    assume(TicketsConfig.MAX_DAYS_IN_BIG_HALL < TicketsConfig.MAX_DAYS_IN_THEATER)

    (1 to TicketsConfig.MAX_DAYS_IN_THEATER).map(showDayNumber => {
      val showDayDate = openingDate.plusDays(showDayNumber - 1)
      showDayNumber match {
        case n if n <= TicketsConfig.MAX_DAYS_IN_BIG_HALL => showDayDate -> TicketsConfig.BIG_HALL_CAPACITY
        case _ => showDayDate -> TicketsConfig.SMALL_HALL_CAPACITY
      }
    }).toMap
  }


  def getFirstDayOfSalesInclusive(showDate: LocalDate): LocalDate = {
    val firstDay = showDate.plusDays(TicketsConfig.SALE_STARTING_DAY_DIFF + 1)

    firstDay
  }

  def getLastDayOfSalesInclusive(showDate: LocalDate): LocalDate = {
    val lastDay = showDate.plusDays(TicketsConfig.SALE_ENDING_DAY_DIFF)

    lastDay
  }

}