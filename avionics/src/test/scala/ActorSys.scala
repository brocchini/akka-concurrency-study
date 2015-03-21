import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.fixture
import org.scalatest.fixture.NoArg

object ActorSys {
  val uniqueId = new AtomicInteger(0)
}

class ActorSys(name: String) extends TestKit(ActorSystem(name))
with ImplicitSender with fixture.NoArg {
  def this() = this(
    "TestSystem%05d".format(
      ActorSys.uniqueId.getAndIncrement()))

  def shutdown(): Unit = system.shutdown()

  override def apply() {
    try super.apply()
    finally shutdown()
  }
}

