package zzz.akka.avionics

import akka.actor.{ActorLogging, ActorSystem, Props, Actor}
import scala.concurrent.duration._

object Altimeter {

  // rate of climb changes
  case class RateChange(amount: Float)

  // Sent by the Altimeter at regular intervals
  case class AltitudeUpdate(altitude: Double)

}

class Altimeter extends Actor with ActorLogging
                              with EventSource {

  import Altimeter._

  implicit val ec = context.dispatcher
  val ceiling = 43000
  val maxRateOfClimb = 5000
  var rateOfClimb = 0f
  var altitude = 0d

  var lastTick = System.currentTimeMillis
  val ticker = context.system.scheduler.schedule(
    100.millis, 100.millis, self, Tick)

  // Internal message sent to ourselves to tell us to update the altitude
  case object Tick

  def altimeterReceive: Receive = {

    // Rate of climb changed
    case RateChange(amount) =>
      // Truncate amount to a range of [-1,1]
      rateOfClimb = amount.min(1.0f).max(-1.0f) * maxRateOfClimb
      log info (s"Altimeter changed rate of climb to $rateOfClimb.")
    case Tick =>
      val tick = System.currentTimeMillis
      altitude = altitude + ((tick - lastTick) / 60000.0) *
        rateOfClimb
      lastTick = tick
      sendEvent(AltitudeUpdate(altitude))
  }

  def receive = eventSourceReceive orElse altimeterReceive

  // Kill ticker on stop
  override def postStop(): Unit = ticker.cancel
}