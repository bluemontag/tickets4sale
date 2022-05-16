package model

import config.TicketsConfig
import model.GenreObj.MUSICAL
import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import util.ShowParser

import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

class ShowSpec extends AnyWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterAll {

  private def fixture = new {
    val aTitle: String = "1984"
    val aDateStr: String = "2022-10-13"
    val aGenreStr: String = "DRAMA"

    val queenShow = Show("Queen The Musical", "2022-01-01", "MUSICAL")
  }

  "a show" must {
    "create from string values" in {
      val f = fixture
      val aShow = Show(f.aTitle, f.aDateStr, f.aGenreStr)

      aShow mustBe a[Show]
      aShow.title mustBe f.aTitle

      val openingDate = LocalDate.parse(f.aDateStr, TicketsConfig.DATE_FORMATTER)
      aShow.opening mustBe openingDate

      aShow.genre mustBe GenreObj.DRAMA
    }

    "be on the big hall for 60 days" in {
      val f = fixture

      val day0 = f.queenShow.opening
      val day31 = f.queenShow.opening.plusDays(31)
      val day59 = f.queenShow.opening.plusDays(59)
      val day60 = f.queenShow.opening.plusDays(60)
      val day99 = f.queenShow.opening.plusDays(99)
      val day100 = f.queenShow.opening.plusDays(100)

      val dates = List(day0, day31, day59, day60, day99, day100)
      val diffs = dates.map(f.queenShow.daysFromOpening(_))

      val onBigHall = dates.map(date => f.queenShow.isInBigHall(date))
      diffs.zip(onBigHall) must be(List((0, true), (31, true), (59, true), (60, false), (99, false), (100, false)))
    }

    "be on the small hall from day 61 until day 99" in {
      val f = fixture

      val day0 = f.queenShow.opening
      val day31 = f.queenShow.opening.plusDays(31)
      val day59 = f.queenShow.opening.plusDays(59)
      val day60 = f.queenShow.opening.plusDays(60)
      val day99 = f.queenShow.opening.plusDays(99)
      val day100 = f.queenShow.opening.plusDays(100)

      val dates = List(day0, day31, day59, day60, day99, day100)
      val diffs = dates.map(f.queenShow.daysFromOpening(_))

      val onSmallHall = dates.map(date => f.queenShow.isInSmallHall(date))
      diffs.zip(onSmallHall) must be(List((0, false), (31, false), (59, false), (60, true), (99, true), (100, false)))
    }

    "have a discount after 80 days in theater" in {
      val f = fixture

      val dayBefore = f.queenShow.opening.minusDays(1)
      val day1 = f.queenShow.opening
      val day80 = f.queenShow.opening.plusDays(79)
      val day81 = f.queenShow.opening.plusDays(80)
      val day100 = f.queenShow.opening.plusDays(99)
      val day101 = f.queenShow.opening.plusDays(100)

      val dates = List(dayBefore, day1, day80, day81, day100, day101)
      val diffs = dates.map(f.queenShow.daysFromOpening(_))

      val prices = dates.map(date => f.queenShow.priceForDay(date))
      val regularPrice = TicketsConfig.ticketPriceByGenre(MUSICAL)
      val discount = TicketsConfig.DISCOUNT_PERCENTAGE_FACTOR
      diffs.zip(prices) must be(List((-1, 0.0),
        (0, regularPrice), (79, regularPrice),
        (80, regularPrice * discount), (99, regularPrice * discount),
        (100, 0.0)))
    }

    "open for sale from 25 days before for 20 days (-24 to -5 both inclusive)" in {
      val openingStr = "2022-01-01"
      val anOpeningDate = LocalDate.parse(openingStr)
      val aShow = Show("Mama Mia", openingStr, "MUSICAL")

      val showDate = anOpeningDate.plusDays(30)

      // different query dates for the samme show
      val beforeSales = showDate.minusDays(25)
      val salesBegin = showDate.minusDays(24)
      val inTheMiddle = showDate.minusDays(20)
      val tenDaysBefore = showDate.minusDays(10) // this is the case of the example in PDF
      val salesEndBefore = showDate.minusDays(6)
      val salesEnd = showDate.minusDays(5)
      val salesEndAfter = showDate.minusDays(4)

      val queryDates = List(beforeSales, salesBegin, inTheMiddle, tenDaysBefore, salesEndBefore, salesEnd, salesEndAfter)
      val diffs = queryDates.map(DAYS.between(showDate, _))

      val saleIsOn = queryDates.map( aShow.saleIsOn(_, showDate))
      diffs.zip(saleIsOn) must be(List((-25, false), (-24, true), (-20, true), (-10, true), (-6, true), (-5, true), (-4, false)))
    }

    "have the correct number of tickets on saling dates" in {
      val openingStr = "2022-01-01"
      val anOpeningDate = LocalDate.parse(openingStr)
      val aShow = Show("Mama Mia", openingStr, "MUSICAL")

      val showDate = anOpeningDate.plusDays(30)

      // different query dates for the samme show
      val beforeSales = showDate.minusDays(25)
      val salesBegin = showDate.minusDays(24)
      val salesBeginAfter = showDate.minusDays(23)
      val inTheMiddle = showDate.minusDays(20)
      val tenDaysBefore = showDate.minusDays(10) // this is the case of the example in PDF
      val salesEndBefore = showDate.minusDays(6)
      val salesEnd = showDate.minusDays(5)
      val salesEndAfter = showDate.minusDays(4)

      val queryDates = List(beforeSales, salesBegin, salesBeginAfter, inTheMiddle, tenDaysBefore, salesEndBefore, salesEnd, salesEndAfter)
      val diffs = queryDates.map(d => DAYS.between(showDate, d))

      val availability = queryDates.map(d => aShow.ticketsAvailable(d, showDate))
      diffs.zip(availability) must be(List((-25, 200), (-24, 200), (-23, 190), (-20, 160), (-10, 60), (-6, 20), (-5, 10), (-4, 0)))
    }

    "have 0 tickets available for sale in 60 days" in {
      val openingStr = "2022-01-01"
      val anOpeningDate = LocalDate.parse(openingStr)
      val aShow = Show("Mama Mia", openingStr, "MUSICAL")

      val showDate = anOpeningDate.plusDays(80)
      val queryDate = anOpeningDate.plusDays(20)

      val availability = aShow.ticketsAvailable(queryDate, showDate)
      availability must be(100) // sale does not started yet

      val forSale = aShow.ticketsAvailableForSale(queryDate, showDate)
      forSale must be(0)
    }

    "have 20 days for selling the tickets" in {
      val showDate = LocalDate.parse("2018-08-15")
      val saleStartingDate = Show.getFirstDayOfSalesInclusive(showDate)
      val saleEndingDate = Show.getLastDayOfSalesInclusive(showDate).plusDays(1)
      DAYS.between(saleStartingDate, saleEndingDate) must be (20)
    }

    "have the right ticket price for the example" in {
      val aShow = Show("COMEDY OF ERRORS","2018-07-01","COMEDY")

      val showDate = LocalDate.parse("2018-08-15")
      val saleStartingDate = Show.getFirstDayOfSalesInclusive(showDate)

      val total = 200
      var daysOpen = 0
      (-10 to 35) foreach( day => {
        val queryDate = saleStartingDate.plusDays(day)

        val factor = aShow.getElapsedDaysFactor(queryDate, showDate)
        val tickets = if (day < 0)
                          total
                      else if (day >= 20) 0
                      else {
                        daysOpen = daysOpen + 1 // if open for sale, count
                        total - (factor * 10)
                      }
        val avail = aShow.ticketsAvailable(queryDate, showDate)
        avail must be (tickets)
      })
      daysOpen must be (20)
    }
  }
}
