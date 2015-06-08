package lisa.logeaterep

import akka.actor._
import lisa.endpoint.esb._
import java.net.InetSocketAddress
  

object LogEater extends App {
  val system = ActorSystem("logEater")
  LISAEndPoint.initial(system)
  
  val le = system.actorOf(LogEaterEP.props(List("operationevents")))
  val mc = system.actorOf(MessageConsumerTest.props(List("operationevents", "resourcefold")))
  //le ! LogFile("logs/prodE.csv", ScaniaProductEvents)
  le ! LogFile("logeater/logs/prodEsmall.csv", ScaniaProductEvents)
  
  //val act = system.actorOf(LogEaterEP.props(List("stateevents")))
  //val mc = system.actorOf(MessageConsumerTest.props(List("stateevents")))
  //act ! LogFile("logs/resE.csv", ScaniaResourceEvents)
  //act ! LogFile("logs/resEsmall.csv", ScaniaResourceEvents)
  
  //val le = system.actorOf(LogEaterEP.props(List("machineevents")))
  //val mc = system.actorOf(MessageConsumerTest.props(List("machineevents")))
  //le ! LogFile("logs/hermle_small.log", HermleEvents, '|')
  //val hermleListener = system.actorOf(HermleListener.props(le))
  //val hermle = system.actorOf(TCPClient.props(new InetSocketAddress("localhost", 7878), hermleListener))

  scala.io.StdIn.readLine match {
    case x => system.terminate()
  }

}
