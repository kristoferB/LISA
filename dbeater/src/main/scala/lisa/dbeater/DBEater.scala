package lisa.dbeater

import akka.actor._
import akka.camel._
import org.apache.activemq.camel.component.ActiveMQComponent
import lisa.endpoint.esb._
import lisa.endpoint.message._
import java.sql.Timestamp


object DBEater extends App {
  val system = ActorSystem("dbEater")
  val camel = CamelExtension(system)  
  val amqUrl = s"nio://localhost:61616"
  camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))

  val le = system.actorOf(DBEaterEP.props("positionraw", "positionfilled"))
  //val mc = system.actorOf(lisa.logeaterep.MessageConsumerTest.props(List("productfold2", "positionfold2")))

  le ! "go"


/*  val px = DB.getProductEvents(Timestamp.valueOf("2013-10-01 00:00:00"), Timestamp.valueOf("2013-10-05 00:00:00"))
  val pos = DB.getPositionInfo()
  println(px.size)
  println(pos)*/

  //val le = system.actorOf(LogEaterEP.props(List("operationevents")))
  //val mc = system.actorOf(MessageConsumerTest.props(List("resourcefold")))
  //le ! LogFile("logs/prodE.csv", ScaniaProductEvents)
  //le ! LogFile("logs/prodEsmall.csv", ScaniaProductEvents)
  
  //val act = system.actorOf(LogEaterEP.props(List("stateevents")))
  //val mc = system.actorOf(MessageConsumerTest.props(List("stateevents")))
  //act ! LogFile("logs/resE.csv", ScaniaResourceEvents)
  //act ! LogFile("logs/resEsmall.csv", ScaniaResourceEvents)
  
  //val le = system.actorOf(LogEaterEP.props(List("machineevents")))
  //val mc = system.actorOf(MessageConsumerTest.props(List("machineevents")))
  //le ! LogFile("logs/hermle_small.log", HermleEvents, '|')
  
  
  
  // test of opc
  //val mc = system.actorOf(MessageConsumerTest.props(List("opc1")))
//  val mess = LISAMessage(
//		  "eventName" -> LISAValue("e1"),
//		  "replyTopic" -> LISAValue("opc1"),
//		  "trigger" -> LISAValue("boolis"),
//		  "properties" -> MapPrimitive(Map("p1"-> 1)),
//		  "stateDefinition" -> MapPrimitive(Map(
//				  "boolis" -> LISAValue("Bucket Brigade.Boolean"),
//				  "random" -> "Random.Boolean",
//				  "int" -> "Bucket Brigade.Int4"
//		  ))	  
//  )
//  
//  val opc = system.actorOf(Props(classOf[LISAProducer], "topic:valueregister"))
//  opc ! mess
  

}