//package lisa.messagefold
//
//import akka.actor._
//import lisa.endpoint.message._
//import lisa.endpoint.esb._
//
//
///**
// * Takes operations and fold positions as positions.
// *
// */
//class StateFold(prop: LISAEndPointProperties, positionTopic: String) extends LISAEndPoint(prop) {
//
//  var foldMap: Map[String, ActorRef] = Map.empty
//
//  def receive = {
//    case mess: LISAMessage => {
//      for {
//        p <- mess.getAsString("machineID")
//      } yield {
//        if (!(foldMap contains p))
//          foldMap = foldMap + (p -> context.actorOf(Props(classOf[StateFoldWorker], p)))
//        foldMap(p) ! mess
//      }
//    }
//    case FoldComplete(id, mess) => {
//      foldMap = foldMap - id
//      sendTo(positionTopic) ! mess
//    }
//  }
//}
//
//object StateFold {
//  def props(topics: List[String], stateTopic: String) = Props(classOf[StateFold],
//      LISAEndPointProperties("StateFolder", stateTopic :: topics), stateTopic)
//
//  private def filter(productTopic: String): LISAMessage => Boolean =  _.getTopic != productTopic
//
//}
//
//
//class StateFoldWorker(resID: String) extends Actor {
//  import com.github.nscala_time.time.Imports._
//  var messSeq: List[LISAMessage] = List.empty
//  var timeframe: Interval = null
//  var counter = 0
//
//  def receive = {
//    case mess: LISAMessage => {
//      for {
//        res <- mess.getAsString("machineID")
//        time <- mess.getAsDate("starttime")
//        opMode <- mess.getAsString("operationMode")
//      } yield {
//        if (res == resID){
//          messSeq = mess :: messSeq
//
//          updateInterval(time)
//
//          if (!timeframe.contains(time)){
//            val sort = messSeq sortWith {(a,b) =>
//            	LISAMessage.as(a.getAsDate("starttime")) < LISAMessage.as(b.getAsDate("starttime"))
//            }
//
//            val times = sort.tail.foldLeft((Map[String, Long]("operating"->0, "idle"-> 0, "down"->0, "unavailible"->0, "undefined"->0), sort.head))((b,a)=>{
//              val time = LISAMessage.as(a.getAsDate("starttime"))
//              val prev = LISAMessage.as(b._2.getAsDate("starttime"))
//              val opMode = LISAMessage.as(b._2.getAsString("operationMode"))
//              val newTime = b._1(opMode) + ((prev to time).toDuration.getStandardMinutes)
//              (b._1 + (opMode -> newTime), a)
//            })
//
//            val starttime = LISAMessage.as(sort.head.getAsDate("starttime"))
//            val stoptime = LISAMessage.as(sort.last.getAsDate("starttime"))
//            val opt = times._1("operating")
//            val dt = times._1("down")
//            val it = times._1("idle")
//            val ut = times._1("unavailible")
//
//            val events = sort.foldRight(List[LISAValue]())((a, b)=>{
//              val id = "lisaID" -> LISAValue(a.getAsString("lisaID"))
//              val time = "time" -> LISAValue(a.getAsDate("starttime"))
//              val opMode = "operationMode" -> LISAValue(a.getAsString("operationMode"))
//              val map = (List(id,time, opMode).filter(_._2 != NonePrimitive)) toMap
//
//              LISAValue(map) :: b
//            })
//
//
//
//
//            val lisa = LISAMessage(
//              "lisaID" -> LISAValue(java.util.UUID.randomUUID().toString()),
//              "resourceID"-> LISAValue(resID),
//              "starttime" -> LISAValue(starttime),
//              "stoptime" -> LISAValue(stoptime),
//              "events" -> LISAValue(events),
//              "fold" -> LISAValue(Map(
//                  "operationTime" -> LISAValue(opt),
//                  "downTime" -> LISAValue(dt),
//                  "idleTime" -> LISAValue(it),
//                  "unavailibleTime" -> LISAValue(ut),
//                  "measureDuration" -> LISAValue((starttime to stoptime).toDuration.getStandardMinutes)
//              ))
//
//
//            )
//
//            println(lisa)
//            //sender ! FoldComplete(resID, lisa)
//            messSeq = List(mess)
//            timeframe = null
//            updateInterval(time)
//          }
//        }
//      }
//    }
//  }
//
//  private def extractValues(ls: List[LISAMessage], key: String) = for {
//    lisa <- ls
//    value <- lisa.get(key)
//  } yield value
//
//  def updateInterval(time: DateTime) = {
//    if (timeframe == null){
//            val startOfDay = time.withTimeAtStartOfDay()
//            timeframe = startOfDay to (startOfDay + (24 hours))
//          }
//  }
//
//
//}
