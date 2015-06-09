package scania

import lisa.endpoint.esb._
import akka.actor._
import lisa.endpoint.message.LISAMessage


object ScaniaTransform extends App {
  val system = ActorSystem("Scania")
  LISAEndPoint.initial(system)

  //val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.scania.consumeTopics")
  val trans = system.actorOf(ScaniaTransformEP.props(List("Product_Status","Machine_Status"), List("Events")))
  val messL = system.actorOf(lisa.logeaterep.MessageConsumerTest.props(List("Events")))




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

case class tempMS(Mode_of_Operation: String,
                   Cycle_timer_value: String,
                   Cycle_time_number: String,
                   Machine_ID: String,
                   Machine_Status: String)

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
        val y = mess.getAs[tempMS]

        val newMess = x.map(v => LISAMessage(
          "Product_ID" ->v.Product_ID,
          "Product_Type" -> tryO(v.Product_Type.toInt),
          "Product_Status" -> v.Product_Status,
          "Event_Position" -> tryO(v.Event_Position.toInt),
          "Machine_Fixture" -> tryO(v.Machine_Fixture.toInt),
          "timestamp" -> time
        ))
        val newMessY = y.map(v => LISAMessage(
          "Mode_of_Operation" -> tryO(v.Mode_of_Operation.toInt),
          "Cycle_timer_value" -> tryO(v.Cycle_timer_value.toInt),
          "Cycle_time_number"  -> tryO(v.Cycle_time_number.toInt),
          "Machine_ID" -> v.Machine_ID,
          "Machine_Status" -> v.Machine_Status,
          "timestamp" -> time

        ))
      newMess.foreach(m => println("we got at newMess " + m))
      newMessY.foreach(m => println("we got at newMess " + m))
      println(y)
      if (newMess == None && newMessY == None)println(mess)

        newMess.foreach(topics ! _)
        newMessY.foreach(topics ! _)
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

