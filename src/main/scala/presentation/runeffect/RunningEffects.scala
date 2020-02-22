package presentation.runeffect

import zio.DefaultRuntime

import zio.ZIO
import zio.UIO



object RunningEffects {
  val prog = for {
    flightService <- ZIO.environment[FlightService].map(_.flightService)
    z             <- flightService.getFlightPlan(3)
  } yield z.callsign

  val env = new FlightPlanService.Live with FlightService.Live {}

  def main(args: Array[String]): Unit = {
    val runtime = new DefaultRuntime {}
    val appLogic: ZIO[Any, Nothing, String] = prog
      .provide(env)
      .foldM(
        error => UIO.succeed(s"Execution failed with: $error"),
        callsign => UIO.succeed(s"Callsign was $callsign")
      )
    val out = runtime.unsafeRun(appLogic)
    println(out)
  }
}
