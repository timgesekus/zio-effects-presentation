package presentation.atm
import zio.Has
import zio.ZIO

package object atm {

  import zio.ZLayer
  type FlightService     = Has[FlightService.Service]
  type FlightPlanService = Has[FlightPlanService.Service]

  object FlightService {
    trait Service {
      def getFlightPlan(flightId: Int): ZIO[Any, Exception, FlightPlan]
      def getFlight(flightId: Int): ZIO[Any, Exception, Flight]
    }
  val live = ZLayer.fromService[FlightPlanService, FlightService] { flightPlanService:FlightPlanService =>
    Has(
    new Service {
     def getFlight(flightId: Int): ZIO[Any, Exception, Flight] =
        if (flightId == 1) {
          ZIO.succeed(Flight(1, 2, 3))
        } else {
          ZIO.fail(new IllegalArgumentException("Unkown Flight"))
        }
      def getFlightPlan(flightId: Int): ZIO[FlightPlanService, Exception, FlightPlan] =
        for {
          flight     <- getFlight(flightId)
          flightPlan <- flightPlanService.getFlightPlan(flight.flightplanId)
        } yield flightPlan
    })
  }
  }

  object FlightPlanService {
    trait Service {
         def getFlightPlan(flightPlanId: Int): ZIO[Any, Exception, FlightPlan]
    }
  }

  val any  = ZLayer.requires[FlightPlanService with FlightPlanService]
  

}
