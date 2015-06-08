//package evah.folds
//
//import akka.actor._
//import lisa.endpoint.message._
//import lisa.endpoint.esb._
//
//
///**
// * Takes operations and fold them based on productid.
// *
// */
//class PatientFold(prop: LISAEndPointProperties, patientTopic: String) extends LISAEndPoint(prop) {
//
//  var foldMap: Map[String, ActorRef] = Map.empty
//
//  def receive = {
//    case mess: LISAMessage => {
//      for {
//        p <- mess.getAsString("visitID")
//      } yield {
//        if (!(foldMap contains p))
//          foldMap = foldMap + (p -> context.actorOf(Props(classOf[FoldWorker], p)))
//
//        foldMap(p) ! mess
//      }
//    }
//    case FoldComplete(id, mess) => {
//      foldMap = foldMap - id
//      sendTo(patientTopic) ! mess
//    }
//  }
//}
//
//object PatientFold {
//  def props(topics: List[String], patientTopic: String) = Props(classOf[PatientFold],
//      LISAEndPointProperties("PatintFolder", patientTopic :: topics, filter(patientTopic)), patientTopic)
//
//  private def filter(patientTopic: String): LISAMessage => Boolean =  _.getTopic != patientTopic
//
//}
//
//private case class FoldComplete(prodID: String, result: LISAMessage)
//
//private class FoldWorker(visitID: String) extends Actor {
//
//  var messSeq: List[LISAMessage] = List.empty
//
//  def receive = {
//    case mess: LISAMessage => {
//      for {
//        optype <- mess.getAsString("operationType")
//        start <- mess.getAsDate("starttime")
//      } yield {
//        messSeq = mess :: messSeq
//        if (optype == false && !messSeq.isEmpty){
//          import com.github.nscala_time.time.Imports._
//          val sort = messSeq sortWith {(a,b) =>
//            LISAMessage.as(a.getAsDate("starttime")) < LISAMessage.as(b.getAsDate("starttime"))
//          }
//          val eventSeq = extractValues(sort, "lisaID")
//          val posSeq = extractValues(sort, "position")
//          val posSeqTime = extractValues(sort, "starttime")
//          val startTime = LISAMessage.as(sort.head.getAsDate("starttime"))
//          val stopTime = LISAMessage.as(mess.getAsDate("starttime"))
//          val lead = startTime to stopTime toDuration
//
//          val zip = eventSeq zip (posSeq zip posSeqTime)
//          val mappy = zip map(z => {
//        	  val e = LISAMessage.as(z._1.asString)
//        	  val pos = z._2._1
//        	  val t = z._2._2
//        	  MapPrimitive(Map("id"->e,"position"->pos, "time"->t))
//          })
//
//
//
//
//          val lisa = LISAMessage(
//              "lisaID" -> LISAValue(java.util.UUID.randomUUID().toString()),
//              "productID"-> LISAValue(visitID),
//              "events" -> LISAValue(mappy),
//              "starttime" -> LISAValue(startTime),
//              "stoptime" -> LISAValue(stopTime),
//              "leadtime" -> LISAValue(startTime to stopTime toDuration)
//          )
//          sender ! FoldComplete(visitID, lisa)
//        }
//      }
//
//    }
//  }
//
//  private def extractValues(ls: List[LISAMessage], key: String) = for {
//    lisa <- ls
//    value <- lisa.get(key)
//  } yield value
//}
