package lisa.logeaterep

import akka.actor._
import akka.camel._
import org.apache.activemq.camel.component.ActiveMQComponent
import scala.concurrent.duration._
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import lisa.endpoint.message._
import lisa.endpoint.esb._
 
class ActiveMQTest(_system: ActorSystem) extends TestKit(_system) 
	with ImplicitSender 
	with WordSpecLike
	with MustMatchers 
	with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("ActiveMQTest"))
  
  val camel = CamelExtension(system)
      
  val amqUrl = s"nio://localhost:61616"
  camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  
  "A Producer and consumer" must {
    val cons = system.actorOf(Props(classOf[LISAConsumer], "topic:foo.bar"))
    val prod = system.actorOf(Props(classOf[LISAProducer], "topic:foo.bar"))
    
    "send and receive a LISA message" in {
      
      //val simpleConsumer = system.actorOf(Props[LISAEndpoint])
      
      val probe = akka.testkit.TestProbe()
      cons ! Listen(probe.ref)
   
      val m2: LISAMessage =  LISAMessage("a1" -> LISAValue("hej")) addHeader("h1",1)
      prod ! m2
      probe.expectMsgPF() { case LISAMessage(b,h) => b.get("a1") == Some("hej") && h.get("h1") == 1 }
    }
    
    
    "not receive a LISA message with filter" in {
      
      val probe2 = akka.testkit.TestProbe()
      val filter = (mess: LISAMessage) => mess.body.contains("a7")
      cons ! Listen(probe2.ref, filter)
 
      val mess = LISAMessage("a2" -> LISAValue("hej"))
      prod ! mess  
      probe2.expectNoMsg(500 millisecond)
    }
  }
}// yield now.value < x  
//           println(t)
//           println(now)
//           t
//        }
