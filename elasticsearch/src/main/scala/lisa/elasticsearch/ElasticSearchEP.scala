package lisa.elasticsearch

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._
import lisa.endpoint.message.MessageLogic._


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

  var i = 0

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
      index = topic, `type` = "LISAMessage", id = id,
      data = mess.bodyToJson, refresh = false
    )

    println(i)
    i += 1

  }


}

object ElasticSearchEP {
  def props(topics: List[String]) = Props(classOf[ElasticSearchEP], LISAEndPointProperties("MessageConsumerTest", topics))

}


case class ReadFromES(index: String, isa: Option[String])

class ElasticOutEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  val conf = com.typesafe.config.ConfigFactory.load.getConfig("lisa.elastic")
  val elasticIP = conf.getString("ip")
  val elasticPort = conf.getInt("port")

  import scala.concurrent.Await
  import scala.concurrent.duration._
  import context.dispatcher
  import wabisabi._

  println(elasticIP)
  val client = new Client(s"http://$elasticIP:$elasticPort")

  var i = 0

  def receive = {
    case ReadFromES(index, isa) => {
      getFromES(index, isa)
    }
  }

  import lisa.endpoint.message.MessageLogic._
  def getFromES(index: String, isa: Option[String]) = {

    val searchResponse = client.search(index = index,  query = "{\"query\": { \"match_all\": {} }}",
      uriParameters = SearchUriParameters(scroll = Some("1m"), searchType = Some(Scan)), `type`= isa)

    println(i)
    i += 1

  }


}

object ElasticOutEP {
  def props(topics: List[String]) = Props(classOf[ElasticSearchEP], LISAEndPointProperties("MessageConsumerTest", List(), topics))

}


