package presentation.runeffect
import zio.ZIO

trait FlightPlanService {
  val flightPlanService: FlightPlanService.Service[Any]
}

object FlightPlanService {
  trait Service[R] {
    def getFlightPlan(flightPlanId: Int): ZIO[R, Exception, FlightPlan]
  }
  object > extends Service[FlightPlanService] {
    def getFlightPlan(flightPlanId: Int): ZIO[FlightPlanService,  Exception, FlightPlan] =
      ZIO.accessM(_.flightPlanService.getFlightPlan(flightPlanId: Int))
  }
  trait Live extends FlightPlanService {
    final val flightPlanService = new FlightPlanService.Service[Any] {
      def getFlightPlan(flightPlanId: Int): ZIO[Any, Nothing, FlightPlan] = ZIO.succeed(FlightPlan(5, "Test"))
    }
  }
}
