package zzz.akka.avionics

import akka.actor.{Props, ActorRef, Actor}

trait AttendantCreationPolicy {
  val numberOfAttendants: Int = 8

  def createAttendant: Actor = FlightAttendant()
}

// Provide mechanism for altering how we create the LeadFlightAttendant
trait LeadFlightAttendantProvider {
  def newLeadFlightAttendant: Actor = LeadFlightAttendant()
}

object LeadFlightAttendant {

  case object GetFlightAttendant

  case class Attendant(a: ActorRef)

  def apply() = new LeadFlightAttendant
    with AttendantCreationPolicy
}

class LeadFlightAttendant extends Actor {
  this: AttendantCreationPolicy =>

  import LeadFlightAttendant._

  override def preStart(): Unit = {
    import scala.collection.JavaConverters._
    val attendantNames =
      context.system.settings.config.getStringList(
        "zzz.akka.avionics.flightcrew.attendantNames").asScala
    attendantNames take numberOfAttendants foreach { name =>
      // We create the actors within our context such that
      // they are children of this Actor
      context.actorOf(Props(createAttendant), name)
    }
  }

  // 'children' is an Iterable. This method returns a random one
  def randomAttendant(): ActorRef = {
    context.children.take(scala.util.Random.nextInt(numberOfAttendants) + 1).last
  }

  def receive = {
    case GetFlightAttendant =>
      sender ! Attendant(randomAttendant())
    case m =>
      randomAttendant() forward m
  }

}