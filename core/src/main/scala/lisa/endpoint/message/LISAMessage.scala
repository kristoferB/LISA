package lisa.endpoint.message


import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._
import com.github.nscala_time.time.Imports._

case class LISAMessage(body: JObject, header: Map[String, Any] = Map())

object MessageLogic {

  trait LISAFormats extends DefaultFormats {
    override val typeHintFieldName = "isa"
    override val customSerializers: List[Serializer[_]] = org.json4s.ext.JodaTimeSerializers.all :+ org.json4s.ext.UUIDSerializer
    override val dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  }
  def formats = new LISAFormats {}

  def timeStamp = {
    implicit val f = new LISAFormats {}
    Extraction.decompose(org.joda.time.DateTime.now)
  }

  implicit class extendsJson(json: JValue) {
    def getAs[T](implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      tryWithOption(
        json.extract[T]
      )
    }

  }
  implicit class messLogic(mess: LISAMessage) {
    def +[T](kv: (String, T)*)(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      val j = kv map(x => x._1 -> Extraction.decompose(x._2))
      mess.copy(body = mess.body.copy(obj = mess.body.obj ++ j))
    }
    def +(xs: JObject) = {
      mess.copy(body = mess.body.copy(obj = mess.body.obj ++ xs.obj))
    }
    def addHeader(kv: (String, Any)) = {
      mess.copy(header = mess.header + kv)
    }


    def getAs[T](key: String)(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      val res = mess.body \ key
      tryWithOption(
        res.extract[T]
      )
    }
    def find(key: String) = mess.body \\ key match {
      case JObject(xs) => xs.map(_._2)
      case x: JValue => List(x)
    }
    def findAs[T](key: String)(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      find(key).map(_.extract[T])
    }
    def findObjectsWithKeys(keys: List[String]) = {
      mess.body.filterField {
        case JField(key, JObject(xs)) => {
          val inObj = xs.map(_._1).toSet
          keys.forall(inObj contains)
        }
        case _ => false
      }
    }
    def findObjectsWithKeysAs[T](keys: List[String])(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      for {
        value <- findObjectsWithKeys(keys)
        t <- tryWithOption(value._2.extract[T])
      } yield (value._1, t)
    }
    def findObjectsWithField(fields: List[JField]) = {
      mess.body.filterField {
        case JField(key, JObject(xs)) => {
          fields.forall(xs contains)
        }
        case _ => false
      }
    }
    def findObjectsWithFieldAs[T](fields: List[JField])(implicit formats : org.json4s.Formats, mf : scala.reflect.Manifest[T]) = {
      for {
        value <- findObjectsWithField(fields)
        t <- tryWithOption(value._2.extract[T])
      } yield (value._1, t)
    }
    def getTopic: String = {
      tryWithOption(mess.header("JMSDestination").asInstanceOf[javax.jms.Topic]) match {
        case Some(t) => t.getTopicName()
        case None => ""
      }
    }
  }


  def tryWithOption[T](t: => T): Option[T] = {
    try {
      Some(t)
    } catch {
      case e: Exception => None
    }
  }
}





//object DatePrimitive {
//  def stringToDate(s: String, pattern: String = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"): Option[DateTime] = {
//     val fmt = org.joda.time.format.DateTimeFormat.forPattern(pattern)
//     try
//       Some(fmt.parseDateTime(s))
//     catch {
//       case e:Exception => None
//     }
//  }
//  //import org.json4s.native.Serialization.{read, write}
//  implicit val formats = org.json4s.DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
//
//  def now = Extraction.decompose(DateTime.now)
//}
