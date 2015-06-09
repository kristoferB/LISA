package lisa.elasticsearch

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._
import lisa.endpoint.message.MessageLogic._


/**
 * Takes operations and fold them based on productid.
 *
 */
class ElasticSearchEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.elastic")
  val elasticIP = conf.getString("ip")
  val elasticPort = conf.getInt("port")

  import scala.concurrent.Await
  import scala.concurrent.duration._
  import context.dispatcher
  import wabisabi._

  println(elasticIP)
  val client = new Client(s"http://$elasticIP:$elasticPort")

  def receive = {
    case mess: LISAMessage => {
    	sendToES(mess)
    }
  }

  import lisa.endpoint.message.MessageLogic._
  def sendToES(mess: LISAMessage) = {
    val topic = mess.getTopic
    val id = mess.getAs[String]("lisaID")

    client.index(
      index = "lisa", `type` = topic, id = id,
      data = mess.toJson, refresh = false
    )

  }





}

object ElasticSearchEP {
  def props(topics: List[String]) = Props(classOf[ElasticSearchEP], LISAEndPointProperties("MessageConsumerTest", topics))

}


