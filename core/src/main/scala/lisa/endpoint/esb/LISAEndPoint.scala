package lisa.endpoint.esb

import akka.actor._
import akka.camel._
import akka.event.Logging
import org.apache.activemq.camel.component.ActiveMQComponent

import lisa.endpoint.message._

/**
 * This is the Scala endpoint that you can use as an endpoint for the LISA ESB
 * 
 * To use it the following lines needs to be states when setting up the actor system:
 * 
 * val amqUrl = s"nio://localhost:61616"  // an example. use the activeMQ ip
 * camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))
 * 
 * implement the:
 * def receive = {
 *  case LISAMessage(body, header) => ...
 * }
 * 
 */
abstract class LISAEndPoint(prop : LISAEndPointProperties) extends Actor{
  
  val logg = Logging(context.system, this)
     
  val camel = CamelExtension(context.system)
  private val topics = prop.topics map {(topic) =>
    val c = context.actorOf(Props(classOf[LISAConsumer], "topic:"+ topic))
    val p = context.actorOf(Props(classOf[LISAProducer], "topic:"+ topic))
    c ! Listen(self, prop.messageFilter)
    topic -> ProdCons(p, c)
  } toMap
  
  def sendTo(topicName: String): ActorRef = {
    topics(topicName).producer
  }
  
  def send: ProducerHolder = {
    val p = topics map (_._2.producer)
    ProducerHolder(p.toList)
  }

  private var tempTopicMap: Map[String, ActorRef] = Map.empty
  /**
   * This sender method is used for producing messages to a topic not 
   * registered during construction of the endpoint
   */
  def produceToTopic(topic: String) = {
    if (!tempTopicMap.contains(topic)){
      tempTopicMap = tempTopicMap + (topic ->context.actorOf(Props(classOf[LISAProducer], "topic:"+ topic)))
    }
    tempTopicMap(topic)
  }
  
  private case class ProdCons(producer: ActorRef, consumer: ActorRef)
}

/**
 * Two classes to register for a consumer
 */
case class Listen(ref: ActorRef, messageFilter: (LISAMessage => Boolean) = _ => true)
case class UnListen(ref: ActorRef)


/**
 * Case class to wrap multiple producers to enable sending with !
 */
case class ProducerHolder(producers: List[ActorRef]) {
  def !(l: LISAMessage) = {
    producers foreach (_ ! l)
  }
}
