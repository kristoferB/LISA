package lisa.elasticsearch

import akka.actor._
import lisa.endpoint.esb._
  

object ElasticSearchFiller extends App {
  val system = ActorSystem("ElasticSearchEP")

  LISAEndPoint.initial(system)
  val mc = system.actorOf(ElasticSearchEP.props(List("akutenpatients", "akutenevents", "raw")))
  //val my = system.actorOf(ElasticStateEP.props(List("product"),"prodID"))

  scala.io.StdIn.readLine match {
    case x => system.terminate()
  }
}