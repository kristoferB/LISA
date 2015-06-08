package lisa.messagefold

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._
import org.json4s._
import lisa.endpoint.message.MessageLogic._


/**
 * Takes operations and fold them based on productid.
 *
 */
class ProductFold(prop: LISAEndPointProperties, completeOperationType: String) extends LISAEndPoint(prop) {
  
  var foldMap: Map[String, ActorRef] = Map.empty
  
  def receive = {
    case mess: LISAMessage => {
      for {
        p <- mess.getAs[String]("productID")
      } yield {
        if (!(foldMap contains p))
          foldMap = foldMap + (p -> context.actorOf(Props(classOf[FoldWorker], p, completeOperationType)))
        
        foldMap(p) ! mess
      }
    }
    case FoldComplete(id, mess) => {
      foldMap = foldMap - id
      send ! mess
    }
  }
}

object ProductFold {
  def props(consumeT: List[String], producT: List[String], completeOperationType: String) =
    Props(classOf[ProductFold],
      LISAEndPointProperties("ProductFolder", consumeT, producT), completeOperationType)
}

private case class FoldComplete(prodID: String, result: LISAMessage)

private class FoldWorker(prodID: String, completeOperationType: String) extends Actor {
  import com.github.nscala_time.time.Imports._
  
  var messSeq: List[LISAMessage] = List.empty
  
  def receive = {
    case mess: LISAMessage => {
      for {
        optype <- mess.getAs[String]("operationType")
        start <- mess.getAs[DateTime]("starttime")
      } yield {
        messSeq = mess :: messSeq
        if (optype == completeOperationType && !messSeq.isEmpty){
          import com.github.nscala_time.time.Imports._
          val sort = messSeq sortWith {(a,b) =>
            a.getAs[DateTime]("starttime").get < b.getAs[DateTime]("starttime").get
          }
          val eventSeq = extractValues(sort, "lisaID")
          val posSeq = extractValues(sort, "position")
          val posSeqTime = extractValues(sort, "starttime")
          val startTime = sort.head.getAs[DateTime]("starttime").get
          val stopTime = (mess.getAs[DateTime]("starttime")).get
          val lead = startTime to stopTime toDuration 
          
          val zip = eventSeq zip (posSeq zip posSeqTime)
          val mappy = zip map(z => {
        	  val e = z._1
        	  val pos = z._2._1
        	  val t = z._2._2
        	  Map("id"->e,"position"->pos, "time"->t)
          })
          
          
          
          
          val lisa = LISAMessage(
              "lisaID" -> (java.util.UUID.randomUUID().toString()),
              "productID"-> (prodID),
              "events" -> (mappy),
              "starttime" -> (startTime),
              "stoptime" -> (stopTime),
              "leadtime" -> (startTime to stopTime toDuration)
          )
          sender ! FoldComplete(prodID, lisa)
        }
      }
      
    }
  }
  
  private def extractValues(ls: List[LISAMessage], key: String): List[JValue] = for {
    lisa <- ls
  } yield lisa.get(key)
}
