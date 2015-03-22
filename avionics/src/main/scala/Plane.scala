package zzz.akka.avionics

import akka.actor.{Props, Actor, ActorLogging}
import zzz.akka.avionics.Altimeter.AltitudeUpdate
import zzz.akka.avionics.EventSource.RegisterListener
import zzz.akka.avionics.Plane.GiveMeControl
import zzz.akka.avionics.{ControlSurfaces, Altimeter}

object Plane {

  case object GiveMeControl

}

class Plane extends Actor with ActorLogging {
  val altimeter = context.actorOf(Props(Altimeter()), "Altimeter")
  val controls = context.actorOf(Props(new ControlSurfaces(altimeter)), "ControlSurfaces")

  override def receive: Receive = {
    case GiveMeControl =>
      log info ("Plane giving control.")
      sender ! controls
    case AltitudeUpdate(altitude) =>
      log info (s"Altitude is now $altitude")
  }

  override def preStart() {
    altimeter ! RegisterListener(self)
  }
}