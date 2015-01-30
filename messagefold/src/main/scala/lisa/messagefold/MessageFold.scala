package lisa.messagefold

import akka.actor._
import akka.camel._
import org.apache.activemq.camel.component.ActiveMQComponent
import lisa.endpoint.esb._
  

object MessageFold extends App {
  val system = ActorSystem("messageFold")
  val camel = CamelExtension(system)  
  val amqUrl = s"nio://localhost:61616"
  camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))
  
  //val mc = system.actorOf(ProductFold.props(List("operationevents"), "productfold", "merge"))
  
  //val rc = system.actorOf(PositionFold.props(List("operationevents"), "resourcefold"))
  
  val sf = system.actorOf(StateFold.props(List("stateevents"), "statefold"))

}