package model

import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import util.ShowParser

class ShowParserSpec extends AnyWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterAll {

  private def fixture = new {
    val aTitle: String = "1984"
    val aDateStr: String = "2022-10-13"
    val aGenreStr: String = "DRAMA"

    val queenShow = Show("Queen The Musical", "2022-01-01", "MUSICAL")
  }

  "show parser" must {
    "parse a well-formatted CSV file" in {
      val shows = ShowParser.parseShowFile("src/test/resources/shows-22_23.csv")

      val pigsShow = Show("\"THREE LITTLE PIGS, THE \"", "2022-11-07", "MUSICAL")
      val wonderShow = Show("WONDER.LAND", "2023-01-20", "MUSICAL")

      shows must contain(pigsShow)
      shows must contain(wonderShow)
    }

    "return an empty list when the file does not exist" in {

      val shows = ShowParser.parseShowFile("wrong file name")

      shows mustBe List.empty
    }
  }
}