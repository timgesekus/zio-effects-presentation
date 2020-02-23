package presentation.atm

import zio.ZIO
import zio.Runtime
import zio.console._

object RunEffect {

  val env = FlightService.live ++ FlightPlanService.live

  def prog: ZIO[FlightService with Console, Exception, String] =
    for {
      flightPlan <- FlightService.getFlightPlan(1)
      _          <- putStrLn(s"Callsing is $flightPlan")
    } yield flightPlan.callsign

  def main(args: Array[String]): Unit = {
    val out = Runtime.default.unsafeRun(prog.provideLayer(Console.live ++ liveServices))
    println(s"Output was $out")
  }
}
