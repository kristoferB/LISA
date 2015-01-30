package lisa.messagefold

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._


/**
 * Takes operations and fold positions as positions.
 *
 */
class PositionFold(prop: LISAEndPointProperties, positionTopic: String) extends LISAEndPoint(prop) {
  
  var foldMap: Map[String, ActorRef] = Map.empty
  
  def receive = {
    case mess: LISAMessage => {
      for {
        pos <- mess.getAsInt("position")
      } yield {
        val p = pos.toString
        if (!(foldMap contains p))
          foldMap = foldMap + (p -> context.actorOf(Props(classOf[PositionFoldWorker], p)))
        foldMap(p) ! mess
      }
    }
    case FoldComplete(id, mess) => {
      foldMap = foldMap - id
      sendTo(positionTopic) ! mess
    }
  }
}

object PositionFold {
  def props(topics: List[String], positionTopic: String) = Props(classOf[PositionFold], 
      LISAEndPointProperties("PositionFolder", positionTopic :: topics), positionTopic)
      
  private def filter(productTopic: String): LISAMessage => Boolean =  _.getTopic != productTopic

}


class PositionFoldWorker(resID: String) extends Actor {
  import com.github.nscala_time.time.Imports._
  var messSeq: List[LISAMessage] = List.empty
  var first: DateTime = null
  var last: DateTime = null
  
  def receive = {
    case mess: LISAMessage => {
      for {
        p <- mess.getAsInt("position")
        time <- mess.getAsDate("starttime")
      } yield {
        val pos = p.toString
        if (pos == resID){
          
          messSeq = mess :: messSeq
          sortFirstAndLastDates(time)
          

          if (messSeq.size  > 19){
            val sort = messSeq sortWith {(a,b) =>
            	LISAMessage.as(a.getAsDate("starttime")) < LISAMessage.as(b.getAsDate("starttime"))
            }
            
            val eventSeq = extractValues(sort, "lisaID")
            val productSeq = extractValues(sort, "productID")
            val eventTimeSeq = (extractValues(sort, "starttime"))
            val durationSeq = { 
              val temp = extractValues(sort, "duration")
              if (temp.isEmpty) (for {i <- 0 to sort.size} yield LISAValue(0)) 
              else temp
            }
            
            val startTime = LISAMessage.as(sort.head.getAsDate("starttime"))   
            val l = sort.last
            val stopTime = if (l.getAsDate("stoptime") != None) LISAMessage.as(l.getAsDate("stoptime")) else LISAMessage.as(l.getAsDate("starttime"))
            val timewindow = startTime to stopTime toDuration
            val usetime = durationSeq.foldLeft(0)((a,b)=>b.asInt match {
                case Some(i)=> a+i
                case None => a                            
            })
            //println(resID+ " startTime: "+sort.head.getAsDate("starttime")+" stopTime"+sort.last.getAsDate("stoptime"))
            //println(resID+" usetime: " +usetime+ " timewin: " +timewindow.getStandardSeconds()+ " start: "+startTime+" stop: "+stopTime)

            
            val utilization = usetime.toDouble/timewindow.getStandardSeconds()
              
            
            val events = (eventSeq zip productSeq zip eventTimeSeq zip durationSeq) map {
              case (((a,b), c), d) => MapPrimitive(Map("id"->a,"productID"->b, "time"->c, "duration"->d))
            }

            val lisa = LISAMessage(
              "lisaID" -> LISAValue(java.util.UUID.randomUUID().toString()),
              "resourceID"-> LISAValue(resID),
              "events" -> LISAValue(events),
              "kpi" -> LISAValue(Map(
            		  "starttime" -> LISAValue(startTime),
            		  "stoptime" -> LISAValue(stopTime),
            		  "measureDuration" -> LISAValue(timewindow),
            		  "utilization" -> LISAValue(utilization)
              ))
            )
            sender ! FoldComplete(resID, lisa)
            messSeq = List.empty
          }
        }          
      }      
    }
  }
  
  private def extractValues(ls: List[LISAMessage], key: String): List[LISAValue] = for {
    lisa <- ls
    value <- lisa.get(key)
  } yield value
  
  private def sortFirstAndLastDates(time: DateTime) = {
    if (first == null || first > time) {
      if (last == null) last = first
      first = time
    } else if (last == null || last <= time) last = time

  }
  
  
}
