package zzz.akka.avionics

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{OneForOneStrategy, SupervisorStrategy, SupervisorStrategyConfigurator}

class UserGuardianStrategyConfigurator extends SupervisorStrategyConfigurator {
  override def create(): SupervisorStrategy = {
    OneForOneStrategy(){
      case _ => Resume
    }
  }
}
