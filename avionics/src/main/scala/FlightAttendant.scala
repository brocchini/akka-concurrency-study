package zzz.akka.avionics

import akka.actor.Actor

import scala.concurrent.duration._

trait AttendantResponsiveness {
  val maxResponseTimeMS: Int

  def responseDuration = scala.util.Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {

  case class GetDrink(drinkname: String)

  case class Drink(drinkname: String)

  //Respond within 5 minutes by default
  def apply() = new FlightAttendant
    with AttendantResponsiveness {
    val maxResponseTimeMS = 300000
  }
}

class FlightAttendant extends Actor {
  this: AttendantResponsiveness =>

  import FlightAttendant._

  implicit val ec = context.dispatcher

  def receive = {
    case GetDrink(drinkname) =>
      //We don not repond right away but use the scheduler to ensure we do eventually
      context.system.scheduler.scheduleOnce(responseDuration, sender, Drink(drinkname))
  }
}
