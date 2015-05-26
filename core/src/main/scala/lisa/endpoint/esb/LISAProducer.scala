package lisa.endpoint.esb

import akka.actor._
import akka.camel._
import akka.event.Logging
import org.json4s._
import org.json4s.native.JsonMethods._

import lisa.endpoint.message._

class LISAProducer(topic: String) extends Actor {
  val cprod = context.actorOf(Props(classOf[LISAProducerSender], topic))
  
  val log = Logging(context.system, this)
  
  def receive = {
    case LISAMessage(body, header) => {
      sendCamel(body, header)
    }
  }
  
  def sendCamel(body: JObject, header: Map[String, Any]) = {
    log.debug("Sending to "+topic+" mess:"+ body)
    cprod ! CamelMessage(compact(render(body)), header)
  }
}

object LISAProducer{
  def prop(topic: String) = Props(classOf[LISAProducer], "topic:"+ topic)
}

class LISAProducerSender(topic: String) extends Actor with Producer with Oneway {
	def endpointUri = "activemq:"+topic
}
