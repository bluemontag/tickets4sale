package app

import config.TicketsConfig
import model.Inventory
import util.ShowParser
import zhttp.http._
import zhttp.service.Server
import zio._
import zio.json.EncoderOps

import java.time.LocalDate
import scala.util.Try

object RestApiControllerApp extends ZIOAppDefault {

  val defaultInventoryFile = "src/main/resources/shows-22_23.csv"
  val shows = ShowParser.parseShowFile(defaultInventoryFile)

  // Create HTTP route
  val app: HttpApp[Any, Nothing] = Http.collect[Request] {
    case Method.GET -> !! / "text" => Response.text("Hello World!")
    case Method.GET -> !! / "api" / "shows" / queryDateStr / showDateStr =>

      val queryDate = validateDateParameter(queryDateStr)
      val showDate = validateDateParameter(showDateStr)

      if (queryDate.isFailure || showDate.isFailure) {
        println(s"Bad formatted dates, returning status code 400 BAD_REQUEST")
        Response.status(Status.BAD_REQUEST).setHeaders(Headers(HeaderNames.accessControlAllowOrigin, "*"))
      } else {
        println(s"Processing query for dates:")
        println(s"Query Date: ${queryDate.get}")
        println(s"Show Date: ${showDate.get}")

        val inventory = Inventory(shows, queryDate.get, showDate.get)
        val jsonStr = inventory.toJsonPretty
        val resp = Response.json(jsonStr)
        resp.setHeaders(Headers(HeaderNames.accessControlAllowOrigin, "*"))
      }
  }

  private def validateDateParameter(param: String): Try[LocalDate] = {
    Try(LocalDate.parse(param, TicketsConfig.DATE_FORMATTER))
  }

  // Run it like any simple app
  def run =
    Server.start(8090, app).exitCode
}
