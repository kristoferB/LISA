package lisa.scania

import akka.actor._
import lisa.endpoint.esb._
import lisa.endpoint.message.LISAMessage
import org.joda.time.DateTime


object ScaniaProductFold extends App {
  val system = ActorSystem("Scania")
  LISAEndPoint.initial(system)

  //val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.scania.consumeTopics")
  val trans = system.actorOf(ScaniaProductFoldEP.props(List("Events"), List("Product")))
  //val messL = system.actorOf(lisa.logeaterep.MessageConsumerTest.props(List("Events")))




  scala.io.StdIn.readLine match {
    case x => system.terminate()
  }
}
// we got JObject(List((
// Product_ID,JString(5159S200050)), (
// Product_Type,JString(2)),
// (Product_Status,JString(1000)), (
// Event_Position,JString(178)), (
// Machine_Fixture,JString(1))))


class ScaniaProductFoldEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  import lisa.endpoint.message.MessageLogic._
  var productMap = Map[String,CurrentProduct]()
  def receive = {
    case mess: LISAMessage => {
      val idOption = mess.getAs[String]("Product_ID")
      idOption.foreach(id => {
        val l:List[LISAMessage] = mess :: productMap.get(id).map(_.event).getOrElse(List())
        val status = mess.getAs[String]("Product_Status")
        val position = mess.getAs[Int]("Event_Position").getOrElse(-1)
        val time = mess.getAs[DateTime]("timestamp").get
        val start = productMap.get(id).map(_.time.start).getOrElse(time)
        productMap = productMap + (id-> CurrentProduct(position, ProductTime(start, time), l))
      })
      productMap.foreach(println)
    }
    case x => println(s"Something wrong: $x")
  }

  def tryO[T](t: => T): Option[T] = {
    try {
      Some(t)
    } catch {
      case e: Exception => None
    }
  }
}

object ScaniaProductFoldEP {
  def props(cT: List[String], pT: List[String]) =
    Props(classOf[ScaniaProductFoldEP], LISAEndPointProperties("scaniaProduct", cT, pT))
}

case class ProductTime(start:DateTime, current:DateTime)

case class CurrentProduct(position: Int, time:ProductTime, event:List[LISAMessage])
