package zzz.akka.avionics

import akka.actor.{ActorRef, Props, Actor, ActorLogging}
import zzz.akka.avionics.Altimeter.AltitudeUpdate
import zzz.akka.avionics.EventSource.RegisterListener
import zzz.akka.avionics.Plane.GiveMeControl
import zzz.akka.avionics.{ControlSurfaces, Altimeter}

object Plane {

  // Request to control
  case object GiveMeControl

  // Response to GiveMeControl
  case class Controls(controls: ActorRef)

}

class Plane extends Actor with ActorLogging {
  val cfgstr = "zzz.akka.avionics.flightcrew"
  val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")
  val controls = context.actorOf(Props(new ControlSurfaces(altimeter)), "ControlSurfaces")
  val config = context.system.settings.config

  // crew
  val pilot = context.actorOf(Props[Pilot],
    config.getString(s"$cfgstr.pilotName"))
  val copilot = context.actorOf(Props[Copilot],
    config.getString(s"$cfgstr.copilotName"))
  val autopilot = context.actorOf(Props[Autopilot],
    "AutopilotName")
  val flightAttendant = context.actorOf(Props(LeadFlightAttendant()),
    config.getString(s"$cfgstr.leadAttendantName"))

  override def receive: Receive = {
    case GiveMeControl =>
      log info ("Plane giving control.")
      sender ! controls
    case AltitudeUpdate(altitude) =>
      log info (s"Altitude is now $altitude")
  }

  override def preStart() {
    altimeter ! RegisterListener(self)
    List(pilot,copilot) foreach(_ ! Pilots.ReadyToGo)
  }
}