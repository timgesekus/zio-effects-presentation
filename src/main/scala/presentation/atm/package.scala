package  presentation

package object atm {

import zio.Has

  import zio.ZLayer
  type FlightService     = Has[FlightService.Service]
  type FlightPlanService = Has[FlightPlanService.Service]

  val any = ZLayer.requires[FlightPlanService with FlightPlanService]
  val liveServices =  (FlightPlanService.live >>> FlightService.live) ++ FlightPlanService.live
}
