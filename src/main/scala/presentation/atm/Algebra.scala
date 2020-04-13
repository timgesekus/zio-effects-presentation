package presentation.atm

case class FlightId(id: Int)
case class FlightPlanId(id: Int)
case class TrackId(id: Int)
case class Track(id: TrackId, posx: Double, posy: Double)
case class FlightPlan(id: FlightPlanId, callsign: String)
case class Flight(id: FlightId, flightPlanId: FlightPlanId, trackId: TrackId)
