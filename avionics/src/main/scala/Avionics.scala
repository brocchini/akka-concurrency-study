package zzz.akka.avionics

import java.util.concurrent.TimeUnit
import akka.pattern.ask
import akka.actor.{ActorRef, Props, ActorSystem}
import akka.util.Timeout
import zzz.akka.avionics.ControlSurfaces

import scala.concurrent.Await
import scala.concurrent.duration.{FiniteDuration, Duration}

import scala.concurrent.ExecutionContext.Implicits.global
object Avionics {
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  val system = ActorSystem("PlaneSimulation")
  val plane = system.actorOf(Props[Plane], "Plane")

  def main(args: Array[String]): Unit = {
    val control = Await.result(
      (plane ? Plane.GiveMeControl).mapTo[ActorRef], Duration(5, TimeUnit.SECONDS))

    // Takeoff
    system.scheduler.scheduleOnce(FiniteDuration(200, TimeUnit.MILLISECONDS)){
      control ! ControlSurfaces.StickBack(1f)
    }

    // Level out
    system.scheduler.scheduleOnce(FiniteDuration(1, TimeUnit.SECONDS)){
      control ! ControlSurfaces.StickBack(0f)
    }

    // Climb
    system.scheduler.scheduleOnce(FiniteDuration(3, TimeUnit.SECONDS)){
      control ! ControlSurfaces.StickBack(0.5f)
    }

    // Level out
    system.scheduler.scheduleOnce(FiniteDuration(4, TimeUnit.SECONDS)){
      control ! ControlSurfaces.StickBack(0f)
    }

    // Shut down
    system.scheduler.scheduleOnce(FiniteDuration(5, TimeUnit.SECONDS)){
      system.shutdown
    }

  }
}
