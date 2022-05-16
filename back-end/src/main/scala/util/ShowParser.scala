package util

import config.TicketsConfig
import model.Show

import java.io.{FileNotFoundException, IOException}
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object ShowParser {

  def parseShowFile(filename: String): List[Show] = {
    val shows = ArrayBuffer[Show]()
    try {
      for (line <- Source.fromFile(filename).getLines) {
        val aShow = this.parseShow(line)
        shows.addOne(aShow)
      }
      println(shows.size + " shows parsed successfully.")
      shows.toList
    } catch {
      case m: scala.MatchError =>
        val message = m.getMessage()
        println(s"Error parsing line: $message")
        List()
      case _: FileNotFoundException =>
        println("Couldn't find that file.")
        List()
      case _: IOException =>
        println("Got an IOException!")
        List()
    }
  }

  private def parseShow(line: String): Show = {
    val pattern = TicketsConfig.SHOW_CSV_FILE_COLUMN_PATTERN
    val pattern(title, date, genre) = line

    Show(title, date, genre)
  }
}
