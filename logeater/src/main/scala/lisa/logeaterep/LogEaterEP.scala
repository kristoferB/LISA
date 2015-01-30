package lisa.logeaterep

import akka.actor._
import akka.io.Tcp._
import lisa.endpoint.message._
import lisa.endpoint.esb._
import java.net.InetSocketAddress
import akka.util.ByteString
import akka.event.Logging

case class LogFile(fileName: String, logType: LogType, divider: Char = ';')

sealed abstract trait LogType {
  def convert(list: List[String]): Map[String, LISAValue]
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
      eatFile(file.fileName) foreach {line: String => eatLine(line, file.divider, file.logType)}
      //context.system.shutdown()
    }
    case HermleEvent(line, divider, logType) => {
    	eatLine(line, divider, logType)
    }
  }
  
  def eatLine(l: String, divider: Char, logType: LogType) = {
     val split = (l split divider) map(_.trim()) toList
     val converted = logType.convert(split)
     if (!converted.isEmpty) sendLisa(converted)  	
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

  def sendLisa(map: Map[String, LISAValue]) = {   
    send ! LISAMessage(map)
  }

}

object LogEaterEP {
  def props(topics: List[String]) = Props(classOf[LogEaterEP], LISAEndPointProperties("logEater", topics, _=>false))
  
}

case object ScaniaProductEvents extends LogType {
  def convert(list: List[String]): Map[String, LISAValue] = {
    def req(list: List[String], keys: List[(String, String => Option[LISAValue])]): Map[String, Option[LISAValue]] = {
      if (keys.isEmpty || list.isEmpty) Map[String, Option[LISAValue]]()
      else {
        req(list.tail, keys.tail) + (keys.head._1 -> keys.head._2(list.head))
      }
    }

    val keys: List[(String, String => Option[LISAValue])] = List(
      ("eventID", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("productID", (s: String) => Some(StringPrimitive(s.toUpperCase()))),
      ("eventType", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("position", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("starttime", (s: String) => DatePrimitive.stringToDate(s, "yyyy-MM-dd HH:mm:ss.SSS")),
      ("stoptime", (s: String) => DatePrimitive.stringToDate(s, "yyyy-MM-dd HH:mm:ss.SSS")),
      ("duration", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("comment", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("status", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("opertionStatus", (s: String) => tryWithOption(IntPrimitive(s.toInt))))

    val result = req(list, keys)
    
    val filtered = for {
      k <- result
      v <- k._2
    } yield (k._1 -> v)
    
    //val id = LISAValue(java.util.UUID.randomUUID().toString())
    val id = LISAValue(filtered("eventID").asInstanceOf[IntPrimitive].value.toString)
    
    filtered ++ Map("operationType" -> StringPrimitive(if (filtered.contains("stoptime")) "transport" else "merge"), "lisaID"->id) 
    	
  }

  def tryWithOption[T](t: => T): Option[T] = {
    try {
      Some(t)
    } catch {
      case e: Exception => None
    }
  }
}


case object ScaniaResourceEvents extends LogType {
  def convert(list: List[String]): Map[String, LISAValue] = {
    def req(list: List[String], keys: List[(String, String => Option[LISAValue])]): Map[String, Option[LISAValue]] = {
      if (keys.isEmpty || list.isEmpty) Map[String, Option[LISAValue]]()
      else {
        req(list.tail, keys.tail) + (keys.head._1 -> keys.head._2(list.head))
      }
    }

    val keys: List[(String, String => Option[LISAValue])] = List(
      ("eventID", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("machineID", (s: String) => Some(StringPrimitive(s.toUpperCase()))),
      ("eventType", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("starttime", (s: String) => DatePrimitive.stringToDate(s, "yyyy-MM-dd HH:mm:ss.SSS")),
      ("stoptime", (s: String) => DatePrimitive.stringToDate(s, "yyyy-MM-dd HH:mm:ss.SSS")),
      ("duration", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("comment", (s: String) => tryWithOption(IntPrimitive(s.toInt))),
      ("status", (s: String) => tryWithOption(IntPrimitive(s.toInt)))
      )

    val result = req(list, keys)
    
    val filtered = for {
      k <- result
      v <- k._2
    } yield (k._1 -> v)
    
    //val id = LISAValue(java.util.UUID.randomUUID().toString())
    val id = LISAValue(filtered("eventID").asInstanceOf[IntPrimitive].value.toString)
    
    val operationMode = filtered("status") match {
      case IntPrimitive(i) => i match {
        case 0 => "unavailible"
        case 1 => "operating"
        case 3 => "idle"
        case 5 => "operating"
        case 7 => "idle"
        case 8 => "down"
        case _ => "undefined"
      } case _ => "undefined"
      
    }
    
    filtered ++ Map("operationMode" -> StringPrimitive(operationMode), "lisaID"->id) 
    	
  }

  def tryWithOption[T](t: => T): Option[T] = {
    try {
      Some(t)
    } catch {
      case e: Exception => None
    }
  }
}


case object HermleEvents extends LogType {
  def convert(list: List[String]): Map[String, LISAValue] = {
    def req(list: List[String], result: Map[String, Option[LISAValue]]): Map[String, Option[LISAValue]] = {
      list match {
        case Nil => result
        case x :: Nil => result
        case key :: xs => {
          req(xs.tail, result + (key->Some(xs.head)))
        }
      }
    }
    
    if (list.isEmpty) Map.empty
    else {
      val result = Map("LogTime" -> DatePrimitive.stringToDate(list.head, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
      val map = req(list.tail, result)
      
      val finalMap = for {
        kv <- map
        v <- kv._2 if kv._1 != ""
      } yield kv._1 -> v
      
      if (finalMap.size < 2) Map.empty
      else finalMap
    }
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

case class HermleEvent(line: String, divider: Char, logType: LogType)

class HermleListener(eater: ActorRef) extends Actor {
	val log = Logging(context.system, this)
	
	def receive = {
		case Connected(local, _) => log.info("Connected to: " + local)
		case bytes: ByteString => {
			val message = bytes.utf8String
			eater ! HermleEvent(message, '|', HermleEvents)
		}
		case message: TCPClient.TCPError => {
			log.error("TCP Error: " + message.message)
			context stop self
			context.system.shutdown()
		}
		case message => log.warning("Unhandled: " + message)
	}
}

object HermleListener {
	def props(eater: ActorRef) = Props(classOf[HermleListener], eater)
}

object TCPClient {
	def props(remote: InetSocketAddress, replies: ActorRef) = Props(classOf[TCPClient], remote, replies)
	
	case class TCPError(message: String)
}

class TCPClient(remote: InetSocketAddress, listener: ActorRef) extends Actor {
	import akka.io.{IO, Tcp}
	import context.system
	import Tcp._
	import TCPClient.TCPError
	
	IO(Tcp) ! Connect(remote)

	def receive = {
		case CommandFailed(_: Connect) =>
			listener ! TCPError("Connect failed")
			context stop self
		case c @ Connected(remote, local) =>
			listener ! c
			val connection = sender
			connection ! Register(self)
			context become {
				case data: ByteString =>
					connection ! Write(data)
				case CommandFailed(w: Write) =>
					listener ! TCPError("Write failed")
					context stop self
				case Received(data) =>
					listener ! data
				case "close" =>
					connection ! Close
				case _: ConnectionClosed =>
					listener ! TCPError("Connection closed")
					context stop self
			}
	}
}
