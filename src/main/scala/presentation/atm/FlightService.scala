package presentation.atm

import zio.ZIO
import zio.ZLayer
import zio.Has

object FlightService {
  trait Service {
    def getFlightPlan(flightId: Int): ZIO[Any, Exception, FlightPlan]
    def getFlight(flightId: Int): ZIO[Any, Exception, Flight]
  }
  val live: ZLayer[FlightPlanService, Nothing, Has[FlightService.Service]] = ZLayer.fromService {
    (flightPlanService: FlightPlanService.Service) =>
      new Service {
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
  def getFlight(flightId: Int): ZIO[FlightService, Exception, Flight] =
    ZIO.accessM(_.get.getFlight(flightId))
  def getFlightPlan(flightId: Int): ZIO[FlightService, Exception, FlightPlan] =
    ZIO.accessM(_.get.getFlightPlan(flightId))
}
