package model

import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import util.ShowParser
import zio.json.EncoderOps

import java.time.LocalDate

class InventorySpec extends AnyWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterAll {


  "Inventory processing" must {
    "contain COMEDY OF ERRORS and CATS but not EVERYMAN in Scenario 1" in {
      val queryDate = LocalDate.parse("2018-01-01")
      val showDate = LocalDate.parse("2018-07-01")
      val shows = ShowParser.parseShowFile("src/test/resources/us1.csv")

      val inventory = Inventory(shows, queryDate, showDate)

      val json = inventory.toJsonPretty
      json must include ("{\n      \"genre\" : \"COMEDY\",\n      \"shows\" : [\n        {\n          \"title\" : \"COMEDY OF ERRORS\",\n          \"opening\" : \"2018-07-01\",\n          \"ticketsLeft\" : 200,\n          \"ticketsAvailable\" : 0,\n          \"status\" :  \"Sale Not Started\" ,\n          \"price\" : 50.0\n        }\n      ]\n    }")
      json must include ("{\n      \"genre\" : \"MUSICAL\",\n      \"shows\" : [\n        {\n          \"title\" : \"CATS\",\n          \"opening\" : \"2018-06-01\",\n          \"ticketsLeft\" : 200,\n          \"ticketsAvailable\" : 0,\n          \"status\" :  \"Sale Not Started\" ,\n          \"price\" : 70.0\n        }\n      ]\n    }")
      json must not include ("\"title\" : \"EVERYMAN\"")
    }

    "contain the 3 elements in Scenario 2" in {
      val queryDate = LocalDate.parse("2018-08-01")
      val showDate = LocalDate.parse("2018-08-15")
      val shows = ShowParser.parseShowFile("src/test/resources/us1.csv")

      val inventory = Inventory(shows, queryDate, showDate)

      val json = inventory.toJsonPretty

      json must include ("{\n      \"genre\" : \"DRAMA\",\n      \"shows\" : [\n        {\n          \"title\" : \"EVERYMAN\",\n          \"opening\" : \"2018-08-01\",\n          \"ticketsLeft\" : 100,\n          \"ticketsAvailable\" : 10,\n          \"status\" :  \"Open For Sale\" ,\n          \"price\" : 40.0\n        }\n      ]\n    }")
      json must include ("{\n      \"genre\" : \"COMEDY\",\n      \"shows\" : [\n        {\n          \"title\" : \"COMEDY OF ERRORS\",\n          \"opening\" : \"2018-07-01\",\n          \"ticketsLeft\" : 100,\n          \"ticketsAvailable\" : 10,\n          \"status\" :  \"Open For Sale\" ,\n          \"price\" : 50.0\n        }\n      ]\n    }")
      json must include ("{\n      \"genre\" : \"MUSICAL\",\n      \"shows\" : [\n        {\n          \"title\" : \"CATS\",\n          \"opening\" : \"2018-06-01\",\n          \"ticketsLeft\" : 50,\n          \"ticketsAvailable\" : 5,\n          \"status\" :  \"Open For Sale\" ,\n          \"price\" : 70.0\n        }\n      ]\n    }")
    }
  }
}