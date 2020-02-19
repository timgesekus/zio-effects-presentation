package presentation.runeffect

import zio.ZIO

trait FlightService {
  val flightService: FlightService.Service[Any]
}

object FlightService {
  trait Service[R] {
    def getFlightPlan(flightId: Int): ZIO[R, Exception, FlightPlan]
    def getFlight(flightId: Int): ZIO[R, Exception, Flight]
  }
  object > extends Service[FlightService] {
    def getFlightPlan(flightId: Int): ZIO[FlightService, Exception, FlightPlan] =
      ZIO.accessM(_.flightService.getFlightPlan(flightId: Int))
    def getFlight(flightId: Int): ZIO[FlightService, Exception, Flight] =
      ZIO.accessM(_.flightService.getFlight(flightId: Int))
  }
  trait Live extends FlightService {
    val flightPlanService: FlightPlanService.Service[Any]
    final val flightService = new FlightService.Service[Any] {
      def getFlight(flightId: Int): ZIO[Any, Exception, Flight] =
        if (flightId == 1) {
          ZIO.succeed(Flight(1, 2, 3))
        } else {
          ZIO.fail(new IllegalArgumentException("Unkown Flight"))
        }
      def getFlightPlan(flightId: Int): ZIO[Any, Exception, FlightPlan] =
        for {
          flight     <- getFlight(flightId)
          flightPlan <- flightPlanService.getFlightPlan(flight.flightplanId)
        } yield flightPlan
    }
  }

}
