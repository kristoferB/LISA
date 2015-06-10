//package lisa.logeaterep
//
//import akka.actor._
//import akka.camel._
//import org.apache.activemq.camel.component.ActiveMQComponent
//import scala.concurrent.duration._
//import akka.testkit.TestKit
//import org.scalatest.WordSpecLike
//import org.scalatest.matchers.MustMatchers
//import org.scalatest.BeforeAndAfterAll
//import akka.testkit.ImplicitSender
//import lisa.endpoint.message._
//import lisa.endpoint.esb._
//
//class EndPointTest(_system: ActorSystem) extends TestKit(_system)
//	with ImplicitSender
//	with WordSpecLike
//	with MustMatchers
//	with BeforeAndAfterAll {
//
//  def this() = this(ActorSystem("EndPointTest"))
//
//  val camel = CamelExtension(system)
//
//  val amqUrl = s"nio://localhost:61616"
//  camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))
//
//  override def afterAll {
//    TestKit.shutdownActorSystem(system)
//  }
//
//  "Example Endpoint" must {
//
//    "modify the message" in {
//      import lisa.endpoint.examples._
//      val topics = List("lisa.events", "test")
//      val es = system.actorOf(Props(classOf[ExampleEndPoint], LISAEndPointProperties("example", topics, _=>true)))
//      val cons = system.actorOf(Props(classOf[LISAConsumer], "topic:test"))
//      val cons2 = system.actorOf(Props(classOf[LISAConsumer], "topic:lisa.events"))
//      val prod4 = system.actorOf(Props(classOf[LISAProducer], "topic:lisa.events"))
//
//      import com.github.nscala_time.time.Imports._
//      val now = DatePrimitive.now
//      val probe3 = akka.testkit.TestProbe()
//      cons ! Listen(probe3.ref)
//
//      prod4 ! LISAMessage("operationName" -> LISAValue("o1"), "time" -> now)
//
//      probe3.expectMsgPF() { case LISAMessage(b,h) => b.get("newAttribute") == Some(1) && h.get("headerinfo") == Some("Kalle") &&
//        Some(true) == (for {
//             lv <- b.get("time")
//             x <- lv.asDate
//           } yield now.value < x )
//      }
//    }
//  }
//
//} //yield now.value < x
////           println(t)
////           println(now)
////           t
////        }
