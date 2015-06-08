//package service
//
//import akka.actor.{Props, ActorSystem}
//import akka.io.IO
//import spray.can.Http
//import org.apache.activemq.camel.component.ActiveMQComponent
//import akka.camel._
//import lisa.endpoint.esb._
//import lisa.endpoint.message._
//import org.joda.time.DateTime
//
//
///**
// * Main class for the service actor and can be stopped by hitting the `"e"` key.
// */
//object Boot extends App {
//
//  private def waitForExit() = {
//    def waitEOF(): Unit = Console.readLine() match {
//      case "exit" => system.shutdown()
//      case _ => waitEOF()
//    }
//    waitEOF()
//  }
//
//  // we need an ActorSystem to host our application in
//  implicit val system = ActorSystem("actor-system")
//
//  val amqAddress = ServiceSettings(system).activeMQAddress
//  val amqPort = ServiceSettings(system).activeMQPort
//  val camel = CamelExtension(system)
//  val amqUrl = s"nio://$amqAddress:$amqPort"
//  camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent(amqUrl))
//
//  //val sp = system.actorOf(lisa.sp.SPEP.props(List("raws", "trans", "folds")))
//
//
//  val spfy = system.actorOf(lisa.sp.SPfy.props())
//
//  // create and start our service actor
//  val service = system.actorOf(GUIRouter.props(spfy), "service-actor")
//
//  // start a new HTTP server on port 8080 with our service actor as the handler
//  val interface = ServiceSettings(system).interface
//  val port = ServiceSettings(system).port
//  IO(Http) ! Http.Bind(service, interface, port)
//
//  Console.println(s"Server started ${system.name}, $interface:$port")
//  Console.println("Type `exit` to exit....")
//
//  //test
//
//  waitForExit()
//  system.shutdown()
//
//
//}
//
