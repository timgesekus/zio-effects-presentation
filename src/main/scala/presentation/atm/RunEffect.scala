package presentation.atm

import zio.ZIO
import zio.Runtime
import zio.console._

object RunEffect {

  val env = FlightService.live ++ FlightPlanService.live

  def prog =
    for {
      flightPlan <- ZIO.environment[FlightService].flatMap(_.get.getFlightPlan(1))
      callsign   = flightPlan.callsign
      _          <- putStrLn(s"Callsing is $callsign")
    } yield callsign

  def main(args: Array[String]): Unit = {
    val out = Runtime.default.unsafeRun(prog.provideLayer(Console.live ++ liveServices))
    println(s"Output was $out")
  }
}
