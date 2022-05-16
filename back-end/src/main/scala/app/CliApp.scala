package app

import config.TicketsConfig
import model.{Inventory, ShowResult}
import util.ShowParser
import zio._
import zio.json.{EncoderOps, JsonEncoder}

import java.io.IOException
import java.nio.file.{Files, Paths}
import java.time.LocalDate

object CliApp extends ZIOAppDefault {

  private def enterFileName(): ZIO[Console, Throwable, (Boolean, String)] = {
    for {
      _               <- Console.printLine("Please enter a file name (ENTER for default repository):")
      input           <- Console.readLine
      fileName        = if (input.trim == "") "src/main/resources/shows-22_23.csv" else input
      exists          <- ZIO.attempt(fileExists(fileName))
      message         <- ZIO.attempt(if (exists) s"File found, processing file $fileName" else "File does not exists. Try again.")
      _               <- Console.printLine(message)
    } yield (exists, fileName)
  }

  private def fileExists(fileName: String): Boolean = {
    fileName.trim != "" && Files.exists(Paths.get(fileName))
  }

  private def enterDate(dateName: String): ZIO[Console, Nothing, LocalDate] = {
    for {
      _             <- Console.printLine(s"Enter $dateName in format YYYY-MM-DD:").orDie
      date          <- Console.readLine.orDie
      localDate     <- ZIO.attempt(LocalDate.parse(date, TicketsConfig.DATE_FORMATTER)).orDie
    } yield localDate
  }

  def run = {
    for {
      (_, name)     <- this.enterFileName().repeatUntil( res => res._1)
      shows         <- ZIO.attempt(ShowParser.parseShowFile(name))
      queryDate     <- this.enterDate("query date")
      showDate      <- this.enterDate("show date")
      inventory     <- ZIO.attempt(Inventory(shows, queryDate, showDate))
      result        <- Console.printLine(inventory.toJsonPretty)
    } yield result
  }
}