package presentation.atm

import zio.ZLayer
import zio.ZIO

object FlightPlanService {
  trait Service {
    def getFlightPlan(id: FlightPlanId): ZIO[Any, Exception, FlightPlan]
  }
  // Live implementation of the service as a layer
  // This service does not depend on other services
  def live = ZLayer.succeed {
    new Service {
      final def getFlightPlan(id: FlightPlanId): ZIO[Any, Exception, FlightPlan] =
        ZIO.succeed(FlightPlan(FlightPlanId(4), "TST113"))
    }
  }

  // Provide easy acccess to the service
  def getFlightPlan(id: FlightPlanId): ZIO[FlightPlanService, Exception, FlightPlan] =
    ZIO.accessM(_.get.getFlightPlan(id))
}
