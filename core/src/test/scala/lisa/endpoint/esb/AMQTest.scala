package lisa.endpoint.esb

import akka.actor._
import akka.camel._
import org.apache.activemq.camel.component.ActiveMQComponent
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import lisa.endpoint.message._
import akka.testkit.TestProbe
 
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

  import lisa.endpoint.message.MessageLogic._
 
  
  "A Producer and consumer" must {
    val cons = system.actorOf(Props(classOf[LISAConsumer], "topic:foo.bar"))
    val prod = system.actorOf(Props(classOf[LISAProducer], "topic:foo.bar"))

    "send and receive a LISA message" in {

      //val simpleConsumer = system.actorOf(Props[LISAEndpoint])

      val probe = akka.testkit.TestProbe()
      cons ! Listen(probe.ref)

      val m2: LISAMessage =  LISAMessage("a1" -> "hej") addHeader("h1",1)
      prod ! m2
      probe.expectMsgPF() { case mess @ LISAMessage(b,h) => mess.getAs[String]("a1") == Some("hej") && h.get("h1") == Some(1) }
    }


    "not receive a LISA message with filter" in {
      val probe2 = akka.testkit.TestProbe()
      val filter = (mess: LISAMessage) => mess.contains("a7")
      cons ! Listen(probe2.ref, filter)

      val mess = LISAMessage("a2" -> "hej")
      prod ! mess
      probe2.expectNoMsg(500 millisecond)
    }
  }
}


//{
//           val t = for {
//             lv <- b.get("time")
//             x <- lv.asDate
//           } yield now.value < x  
//           println(t)
//           println(now)
//           t
//        }
