package presentation.atm

import zio.ZIO
import zio.ZLayer
import zio.Has

object FlightService {
  // Define the service as a trait
  trait Service {
    def getFlightPlan(id: FlightId): ZIO[Any, Exception, FlightPlan]
    def getFlight(id: FlightId): ZIO[Any, Exception, Flight]
  }
  // Live implementation of the service as a layer
  // A Layer is a bit like a module in guice
  val live: ZLayer[FlightPlanService, Nothing, Has[FlightService.Service]] = ZLayer.fromService {
    (flightPlanService: FlightPlanService.Service) =>
      new Service {
        def getFlight(id: FlightId): ZIO[Any, Exception, Flight] =
          if (id == FlightId(1)) {
            ZIO.succeed(Flight(FlightId(1), FlightPlanId(2), TrackId(3)))
          } else {
            ZIO.fail(new IllegalArgumentException("Unkown Flight"))
          }
        def getFlightPlan(id: FlightId): ZIO[Any, Exception, FlightPlan] =
          for {
            flight     <- getFlight(id)
            flightPlan <- flightPlanService.getFlightPlan(flight.flightPlanId)
          } yield flightPlan
      }
  }

  // Provide easy acccess to the service
  def getFlight(id: FlightId): ZIO[FlightService, Exception, Flight] =
    ZIO.accessM(_.get.getFlight(id))
  def getFlightPlan(id: FlightId): ZIO[FlightService, Exception, FlightPlan] =
    ZIO.accessM(_.get.getFlightPlan(id))
}
