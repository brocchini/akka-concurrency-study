package zzz.akka.avionics

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestLatch, ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

import scala.concurrent.Await

class AltimeterSpec extends TestKit(ActorSystem("AltimeterSpec"))
with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  import Altimeter._

  override def afterAll() {
    system.shutdown()
  }

  // We'll instantiate a Helper class for every test, making
  // things nicely reusable
  class Helper {

    object EventSourceSpy {
      // the latch gives us fast feedback when something happens
      val latch = TestLatch(1)
    }

    trait EventSourceSpy extends EventSource {
      def sendEvent[T](event: T): Unit =
        EventSourceSpy.latch.countDown()

      // We don't care about processing the messages that
      // EventSource usually processes so we simply don't
      // worry about them
      def eventSourceReceive = Actor.emptyBehavior
    }

    // The slicedAltimeter constructs our Altimeter with
    // the EventSourceSpy
    def slicedAltimeter = new Altimeter with EventSourceSpy

    // This is a helper method that will give us an ActorRef
    // and our plain ol' Altimeter that we can work with
    // directly
    def actor() = {
      val a = TestActorRef[Altimeter](Props(slicedAltimeter))
      (a, a.underlyingActor)
    }
  }

  "Altimeter" should {
    "record rate of climb changes" in new Helper {
      val (_, real) = actor()
      real.receive(RateChange(1f))
      real.rateOfClimb should be(real.maxRateOfClimb)
    }

    "keep rate of climb changes within bounds" in new Helper {
      val (_, real) = actor()
      real.receive(RateChange(1f))
      real.rateOfClimb should be(real.maxRateOfClimb)
    }
    "caculate altitude changes" in new Helper {
      val ref = system.actorOf(Props(Altimeter()))
      ref ! EventSource.RegisterListener(testActor)
      ref ! RateChange(1f)
      fishForMessage() {
        case AltitudeUpdate(altidude) if altidude == 0f =>
          false
        case AltitudeUpdate(altitude) =>
          true
      }
    }
    "send events" in new Helper {
      val (ref, _) = actor()
      Await.ready(EventSourceSpy.latch, 1.second)
      EventSourceSpy.latch.isOpen should be(true)
    }
  }

}
