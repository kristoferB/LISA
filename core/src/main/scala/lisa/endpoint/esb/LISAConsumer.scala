package lisa.endpoint.esb

import akka.actor._
import akka.camel._
import akka.actor.Status.Failure
import akka.event.Logging

import lisa.endpoint.message._



class LISAConsumer(topic: String) extends Actor with Consumer {
  def endpointUri = "activemq:"+topic
  
  val log = Logging(context.system, this)
  
  log.debug("Creating consumer " + LISAConsumer.this)
  log.debug("Creating consumer on" + topic)
    
  
  private var listeners: Set[Listen] = Set()
  
  def receive = {
    case msg: CamelMessage => {
      log.debug("recevied this camelMessage: "+msg)
      msg.body match {
        case lb: LISAMessage => {
          val lisaMessage = LISAMessage(lb.body, msg.headers filter(_._2 != null))
          listeners foreach ((listner)=> {
            if (listner.messageFilter(lisaMessage)) 
              listner.ref ! lisaMessage
          })
        }
        case _ => {
          log.debug("Didn't recevied a LISAMessage")
          sender ! Failure(new Exception("Message " + msg + " is not a LISAMessage"))
        }
      }
    }
    case r: Listen => listeners = listeners + r
    case UnListen(r) => listeners = listeners filter (_.ref != r)
  }
}

object LISAConsumer{
  def prop(topic: String) = Props(classOf[LISAConsumer], "topic:"+ topic)
}