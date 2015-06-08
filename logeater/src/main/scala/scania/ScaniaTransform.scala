package scania

import lisa.endpoint.esb._
import akka.actor._
import lisa.endpoint.message.LISAMessage


object ScaniaTransform extends App {
  val system = ActorSystem("Scania")
  LISAEndPoint.initial(system)

  //val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.scania.consumeTopics")
  val trans = system.actorOf(ScaniaTransformEP.props(List("Product_Status"), List("Product_Event")))
  val messL = system.actorOf(lisa.logeaterep.MessageConsumerTest.props(List("Product_Event")))




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

case class tempPS(Product_ID: String,
                  Product_Type: String,
                  Product_Status: String,
                  Event_Position: String,
                  Machine_Fixture: String)

class ScaniaTransformEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  import lisa.endpoint.message.MessageLogic._
  def receive = {
    case mess: LISAMessage => {
      val time = mess.getHeaderTime

      val x = mess.getAs[tempPS]
      val newMess = x.map(v => LISAMessage(
        "Product_ID" -> mess.getAs[String]("Product_ID"),
        "Product_Type" -> tryO(v.Product_Type.toInt),
        "Product_Status" -> v.Product_Status,
        "Event_Position" -> tryO(v.Event_Position.toInt),
        "Machine_Fixture" -> tryO(v.Machine_Fixture.toInt),
        "timestamp" -> time
      ))

      println("we got at newMess " + newMess)

      newMess.foreach(topics ! _)

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

object ScaniaTransformEP {
  def props(cT: List[String], pT: List[String]) =
    Props(classOf[ScaniaTransformEP], LISAEndPointProperties("scaniaTransformer", cT, pT))
}

