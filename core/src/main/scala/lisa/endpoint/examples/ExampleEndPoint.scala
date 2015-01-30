package lisa.endpoint.examples

import lisa.endpoint.message._
import lisa.endpoint.esb._

/**
 * This is an example of how to implement an endpoint. 
 */
class ExampleEndPoint(prop : LISAEndPointProperties) extends LISAEndPoint(prop) {
  // Will consume from all topics in prop.topics
  def receive = {
    case mess: LISAMessage => {
      val topic = mess.getTopic // The topic the message was sent to
      
      val updatedMessage1 = mess + ("newAttribute" -> 1) addHeader("headerinfo", "kalle")
      
      // Examples
      //sendTo("test") ! mess		// sends message back to a specific topic. throws if topic is not defined
      send ! updatedMessage1 + ("time" -> DatePrimitive.now)		// send mess back to all topics
    }
  }
}
