package lisa.scania
import lisa.endpoint.esb._
import akka.actor._
import lisa.endpoint.message.LISAMessage


class ScaniaTransform extends App {
  val system = ActorSystem("Scania")
  LISAEndPoint.initial(system)

  //val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.scania.consumeTopics")
  val trans = system.actorOf(ScaniaTransformEP.props(List("product_status"), List("product_event")))



  scala.io.StdIn.readLine match {
    case x => system.terminate()
  }
}

class ScaniaTransformEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  def receive = {
    case LISAMessage(body, header) => {
      println(s"we got $body")
    }
  }
}

object ScaniaTransformEP {
  def props(cT: List[String], pT: List[String]) =
    Props(classOf[ScaniaTransformEP], LISAEndPointProperties("scaniaTransformer", cT, pT))
}

