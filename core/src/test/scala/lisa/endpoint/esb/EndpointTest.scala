package lisa.endpoint.esb

import akka.actor._
import akka.camel._
import org.json4s.JsonAST.JObject
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.testkit.TestKit
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender
import lisa.endpoint.message._
import akka.testkit.TestProbe
 
class EndPointTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("EndPointTest"))

  import scala.concurrent.ExecutionContext.Implicits.global

  LISAEndPoint.initial(system)


  import lisa.endpoint.examples._
  val consumetopics = List("lisa.events")
  val producetopics = List("upd")
  val es = system.actorOf(Props(classOf[ExampleEndPoint], LISAEndPointProperties("example", consumetopics, producetopics)))
  val cons = system.actorOf(Props(classOf[LISAConsumer], "topic:upd"), "cons")
  val prod = system.actorOf(Props(classOf[LISAProducer], "topic:lisa.events", JObject()), "prod4")


  override def afterAll = {
    TestKit.shutdownActorSystem(system)
  }

  "Example Endpoint" must {
    import lisa.endpoint.message.MessageLogic._

    "modify the message" in {


      val now = timeStamp
      val probe3 = akka.testkit.TestProbe()
      cons ! Listen(probe3.ref)

      prod ! LISAMessage("operationName" -> "o1", "time" -> now).addHeader(org.apache.activemq.ScheduledMessage.AMQ_SCHEDULED_DELAY -> 3000)
      import com.github.nscala_time.time.Imports._
      probe3.expectMsgPF() { case mess @ LISAMessage(b,h) =>
        println("got message: "+mess)
        mess.getAs[String]("newAttribute") == Some(1) &&
          h.get("headerInfo") == Some("Kalle") &&
          mess.getAs[String]("operationName") == Some("o1") &&
        Some(true) == (for {
             lv <- mess.getAs[DateTime]("time")
           } yield now.getAs[DateTime].map(_ < lv) )
      }
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
