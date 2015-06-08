package lisa.messagefold

import akka.actor._
import akka.camel._
import org.apache.activemq.camel.component.ActiveMQComponent
import lisa.endpoint.esb._
  

object MessageFold extends App {
  val system = ActorSystem("messageFold")
  LISAEndPoint.initial(system)

  
  val mc = system.actorOf(ProductFold.props(List("operationevents"), List("productfold"), "merge"))
  
  //val rc = system.actorOf(PositionFold.props(List("operationevents"), "resourcefold"))
  
  //val sf = system.actorOf(StateFold.props(List("stateevents"), "statefold"))

}