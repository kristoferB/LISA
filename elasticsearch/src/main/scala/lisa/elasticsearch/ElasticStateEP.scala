package lisa.elasticsearch

import akka.actor._
import lisa.endpoint.esb._
import lisa.endpoint.message._


/**
 * Takes operations and fold them based on productid.
 *
 */
class ElasticStateEP(prop: LISAEndPointProperties, keyID: String) extends LISAEndPoint(prop) {
  val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.elastic")
  val elasticIP = conf.getString("ip")
  val elasticPort = conf.getInt("port")

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
    val idO = mess.getAs[String](keyID)

    val t = idO.map{id =>
      client.index(
        index = "state", `type` = topic, id = Some(id),
        data = mess.toJson, refresh = false
      )

    }

    println(s"testing update $t")


  }





}

object ElasticStateEP {
  def props(topics: List[String], key: String) = Props(classOf[ElasticStateEP], LISAEndPointProperties("MessageConsumerTest", topics), key)

}


