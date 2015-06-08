package lisa.logeaterep

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._
import org.json4s._
import lisa.endpoint.message.MessageLogic._

case class LogFile(fileName: String, logType: LogType, divider: Char = ';')

sealed abstract trait LogType {
  def convert(list: List[String]): Option[LISAMessage]

  def tryWithOption[T](t: => T): Option[T] = {
    try {
      Some(t)
    } catch {
      case e: Exception => None
    }
  }
}

/**
 * This EP reads various logfiles and sends out LISAMessages
 * A log file should have one message per line with each value separated by ;
 *
 * In the future we could maybe include a generic header for keys and types, but
 * for now it is hardcoded in the LogType
 *
 */
class LogEaterEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  def receive = {
    case file: LogFile => {
      val lines = eatFile(file.fileName)
      lines foreach { l =>
        val split = (l split file.divider) map(_.trim()) toList
        val convert = file.logType.convert(split)
        convert.foreach(send ! _)
      }

      //context.system.shutdown()
    }
  }

  // maybe change this to handle huge files later
  def eatFile(filename: String): List[String] = {
    import scala.io.Source
    import java.io.{ FileReader, FileNotFoundException, IOException }

    try {
      Source.fromFile(filename).getLines() toList
    } catch {
      case ex: Exception => {
        println("Can not read.")
        println(ex.getMessage())

        context.system.shutdown()

        List()
      }
    }
  }
}

object LogEaterEP {
  def props(produceTopics: List[String]) = Props(classOf[LogEaterEP], LISAEndPointProperties("logEater", List(), produceTopics, _=>false))
}

case object ScaniaProductEvents extends LogType {
  def convert(list: List[String]): Option[LISAMessage] = {
    val keyList = List(
      "eventID",
      "productID",
      "eventType",
      "position",
      "starttime",
      "stoptime",
      "duration",
      "comment",
      "status",
      "opertionStatus"
    )

    val keyValue = for {
      zip <- keyList zip list if zip._2 != "NULL"
      json <- tryWithOption(Extraction.decompose(zip._2))
    } yield zip._1 -> json

    if (keyValue.nonEmpty){
      val keys = keyValue.toMap
      val id = keys("eventID")
      val opType = if (keys.contains("stoptime")) "transport" else "merge"
      Some(LISAMessage(JObject(keyValue)) + ("operationType"-> opType) + ("lisaID"-> id))
    } else None

  }
}


case object ScaniaResourceEvents extends LogType {
  def convert(list: List[String]): Option[LISAMessage] = {
    val keyList = List(
      "eventID",
      "machineID",
      "eventType",
      "starttime",
      "stoptime",
      "duration",
      "comment",
      "status"
    )

    val keyValue = for {
      zip <- keyList zip list if zip._2 != "NULL"
      json <- tryWithOption(Extraction.decompose(zip._2))
    } yield zip._1 -> json

    if (keyValue.nonEmpty){
      val keys = keyValue.toMap
      val id = keys("eventID")

      val operationMode = keys("status") match {
        case JInt(i) if i == 0 => JString("unavailible")
        case JInt(i) if i == 1 => JString("operating"  )
        case JInt(i) if i == 3 => JString("idle"       )
        case JInt(i) if i == 5 => JString("operating"  )
        case JInt(i) if i == 7 => JString("idle"       )
        case JInt(i) if i == 8 => JString("down"       )
        case x =>       JString("undefined"  )
      }

      Some(LISAMessage(JObject(keyValue)) + ("operationMode"-> operationMode) + ("lisaID"-> id))
    } else None
  }


}





/**
2013-09-11T08:37:10.312Z|avail|AVAILABLE
2013-09-11T08:37:10.312Z|logic|WARNING|701939(1)|100||Kabinentür öffnet nicht. Stop C abgebrochen.
2013-09-11T08:37:10.312Z|system|WARNING|120120(1)|100||Summalarm för fel vid åtkomst av larmtexter. Text se förklaring
2013-09-11T08:37:10.312Z|system|WARNING|120120(2)|100||Summalarm för fel vid åtkomst av larmtexter. Text se förklaring
2013-09-11T08:37:10.312Z|exec_1|STOPPED|line_1||mode_1|MANUAL|program_1|_N_CMM_SINGLE_MPF|Fovr_1|1|FRovr_1|90|Sovr_1|0|path_position_1|500.2180000000 1060.0000000000 749.9000000000|tool_id_1|0
2013-09-11T08:37:10.312Z|system_1|NORMAL||||
2013-09-11T08:37:10.312Z|block_1||estop_1|ARMED
2013-09-11T08:37:10.312Z|msg_1||
2013-09-11T08:37:10.312Z|active_axes_1|X1 Y1 Z1 SP1 MAG1 C1 A1 U1 Y2 |part_count_1|0|Fact_1|0|Fcmd_1|0|X1act|500.218|X1load|0.7446516312|Y1act|1060|Y1load|1.049836726|Z1act|749.9|Z1load|25.73931089|SP1act|269.99841|SP1load|0.1525925474|MAG1act|34.28571|MAG1load|0.4974517045|C1act|359.97113|C1load|0.04882961516|A1act|-0.00455|A1load|8.951078829|U1act|-0.00104|U1load|1.913510544|Y2act|1060|Y2load|1.162755211|SP1mode|SPINDLE|SP1direction|CLOCKWISE|SP1velocity|0
2013-09-11T08:37:10.562Z|X1load|0.8270516068|Z1load|25.81255531|SP1act|269.9991|SP1load|0.07019257179|MAG1load|0.4028443251|A1load|9.204382458|U1load|1.858577227|Y2load|1.089510788
2013-09-11T08:37:11.013Z|X1load|0.872829371|Y1load|0.9155552843|Z1load|25.82476272|SP1act|269.99841|SP1load|0.06714072085|MAG1load|0.4425183874|A1load|9.118930631|U1load|1.934873501|Y2load|1.080355235
2013-09-11T08:37:11.474Z|X1load|0.7904293954|Y1load|0.9460737938|Z1load|25.73931089|SP1load|0.08239997559|MAG1load|0.408948027|A1load|8.990752892|U1load|1.925717948|Y2load|1.306192206
 * 
 */

//case class HermleEvent(line: String, divider: Char, logType: LogType)
//
//class HermleListener(eater: ActorRef) extends Actor {
//	val log = Logging(context.system, this)
//
//	def receive = {
//		case Connected(local, _) => log.info("Connected to: " + local)
//		case bytes: ByteString => {
//			val message = bytes.utf8String
//			eater ! HermleEvent(message, '|', HermleEvents)
//		}
//		case message: TCPClient.TCPError => {
//			log.error("TCP Error: " + message.message)
//			context stop self
//			context.system.shutdown()
//		}
//		case message => log.warning("Unhandled: " + message)
//	}
//}
//
//object HermleListener {
//	def props(eater: ActorRef) = Props(classOf[HermleListener], eater)
//}
//
//object TCPClient {
//	def props(remote: InetSocketAddress, replies: ActorRef) = Props(classOf[TCPClient], remote, replies)
//
//	case class TCPError(message: String)
//}
//
//class TCPClient(remote: InetSocketAddress, listener: ActorRef) extends Actor {
//	import akka.io.{IO, Tcp}
//	import context.system
//	import Tcp._
//	import TCPClient.TCPError
//
//	IO(Tcp) ! Connect(remote)
//
//	def receive = {
//		case CommandFailed(_: Connect) =>
//			listener ! TCPError("Connect failed")
//			context stop self
//		case c @ Connected(remote, local) =>
//			listener ! c
//			val connection = sender
//			connection ! Register(self)
//			context become {
//				case data: ByteString =>
//					connection ! Write(data)
//				case CommandFailed(w: Write) =>
//					listener ! TCPError("Write failed")
//					context stop self
//				case Received(data) =>
//					listener ! data
//				case "close" =>
//					connection ! Close
//				case _: ConnectionClosed =>
//					listener ! TCPError("Connection closed")
//					context stop self
//			}
//	}
//}
