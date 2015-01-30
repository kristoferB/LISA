package lisa.endpoint.esb

import akka.actor._
import akka.camel._
import akka.event.Logging

import lisa.endpoint.message._

class LISAProducer(topic: String) extends Actor {
  val cprod = context.actorOf(Props(classOf[LISAProducerSender], topic))
  
  val log = Logging(context.system, this)
  
  def receive = {
    case LISAMessage(body, header) => {
      sendCamel(LISAMessage(body), header)
    }
  }
  
  def sendCamel(body: LISAMessage, header: Map[String, Any]) = {
    log.debug("Sending to "+topic+" mess:"+ body)
    cprod ! CamelMessage(body, header)
  }
}

object LISAProducer{
  def prop(topic: String) = Props(classOf[LISAProducer], "topic:"+ topic)
}

class LISAProducerSender(topic: String) extends Actor with Producer with Oneway {
	def endpointUri = "activemq:"+topic
}
