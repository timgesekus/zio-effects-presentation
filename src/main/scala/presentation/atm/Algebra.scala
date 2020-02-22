package presentation.atm

case class Track(trackId: Int, posx: Double, posy: Double)
case class FlightPlan(flightPlanId: Int, callsign: String)
case class Flight(flightId: Int, flightplanId: Int, TrackId: Int)
