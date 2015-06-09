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

  import com.sksamuel.elastic4s.ElasticClient
  import com.sksamuel.elastic4s.ElasticDsl._
  val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.elastic")
  val elasticIP = conf.getString("ip")
  val elasticPort = conf.getInt("port")


  val client = ElasticClient.remote(elasticIP, elasticPort)

  var count = 0

  prop.consumeTopics foreach { topic =>
    client execute {
      create index topic.toLowerCase()
    }
  }


  def receive = {
    case mess: LISAMessage => {
    	sendToES(mess)
    }
  }

  import lisa.endpoint.message.MessageLogic._
  def sendToES(mess: LISAMessage) = {
    val topic = mess.getTopic.toLowerCase()
    client.execute {
      index into topic fields("body"->mess.bodyToJson, "header"->mess.headerToJson)
      //temp
//      val temp = index into "lisamessage" doc mess
//
//      index into "lisamessage" doc mess
  }
    //println(topic+" no:"+count)
    count += 1
    //log.debug("Sending to ES on : "+topic+" mess:" + mess)

  }


}

object ElasticSearchEP {
  def props(topics: List[String]) = Props(classOf[ElasticSearchEP], LISAEndPointProperties("MessageConsumerTest", topics))

}


