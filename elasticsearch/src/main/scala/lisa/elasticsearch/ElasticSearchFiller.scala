package lisa.elasticsearch

import akka.actor._
import akka.camel._
import org.apache.activemq.camel.component.ActiveMQComponent
import lisa.endpoint.esb._
  

object ElasticSearchFiller extends App {
  val system = ActorSystem("ElasticSearchEP")
  val camel = CamelExtension(system)  
  val amqUrl = s"nio://localhost:61616"
  camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))
  
  val mc = system.actorOf(ElasticSearchEP.props(List("operationevents","productfold", "resourcefold", "stateevents")))
  
}