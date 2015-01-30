package lisa.logeaterep

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._


/**
 * Receives Messages and print them
 *
 */
class MessageConsumerTest(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  def receive = {
    case mess: LISAMessage => {
      println(s"MessageConsumerTest got on ${mess.getTopic}:  $mess")
    }
    case _ => println("got error")
  }
}

object MessageConsumerTest {
  def props(topics: List[String]) = Props(classOf[MessageConsumerTest], LISAEndPointProperties("MessageConsumerTest", topics, _=> true))
}
