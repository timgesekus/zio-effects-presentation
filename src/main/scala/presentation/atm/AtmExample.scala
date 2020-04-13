package presentation.atm

import zio.ZIO
import zio.Runtime
import zio.console._
import zio.UIO

//Import shortcuts
import FlightService._

object AtmExample {

  // Define Live env
  val env = FlightService.live ++ FlightPlanService.live

  def prog: ZIO[FlightService with Console, Exception, String] =
    for {
      flightPlan <- getFlightPlan(FlightId(1))
      _          <- putStrLn(s"Callsign is $flightPlan")
    } yield flightPlan.callsign

    
  def main(args: Array[String]): Unit = {
    val providedProg: ZIO[Any, Exception, String] = prog.provideLayer(Console.live ++ liveServices)
    val errorFreeProg: ZIO[Any, Nothing, String] = providedProg.foldM(
      error => UIO.succeed(s"Execution failed with: $error"),
      callsign => UIO.succeed(s"Callsign was $callsign")
    )
    val out: String = Runtime.default.unsafeRun(errorFreeProg)
    println(s"Output was $out")
  }
}
