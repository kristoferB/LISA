package service

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import spray.can.Http
import lisa.endpoint.esb._


/**
 * Main class for the service actor and can be stopped by hitting the `"e"` key.
 */
object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("actor-system")
  LISAEndPoint.initial(system)

  val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.gui")
  val guiIP = conf.getString("ip")
  val guiPort = conf.getInt("port")

  val spfy = system.actorOf(lisa.sp.SPfy.props())

  // create and start our service actor
  val service = system.actorOf(GUIRouter.props(spfy), "service-actor")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, guiIP, guiPort)

  Console.println(s"Server started ${system.name}, $guiIP:$guiPort")
  Console.println("Type enter to exit....")

  //test

  scala.io.StdIn.readLine match {
    case x => system.terminate()
  }


}

