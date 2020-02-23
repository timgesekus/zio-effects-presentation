package presentation.atm

import zio.ZLayer
import zio.ZIO

object FlightPlanService {
  trait Service {
    def getFlightPlan(flightPlanId: Int): ZIO[Any, Exception, FlightPlan]
  }
  def live = ZLayer.succeed {
    new Service {
      final def getFlightPlan(flightPlanId: Int): ZIO[Any, Exception, FlightPlan] =
        ZIO.succeed(FlightPlan(4, "TST113"))
    }
  }
  def getFlightPlan(flightPlanId: Int): ZIO[FlightPlanService, Exception, FlightPlan] =
    ZIO.accessM(_.get.getFlightPlan(flightPlanId))
}
