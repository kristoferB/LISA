package lisa.endpoint.message

// wrapper for jodatime
import com.github.nscala_time.time.Imports._

/**
 * The LISA messages. Create a message by sending in a body and (optionally) a header,
 * where the body is a map between a attribute name and a LISAValue. The header
 * is in the form of a activeMQ or JMS header, i.e. a map between an attribute name 
 * and any object
 * 
 * Alternative constructions is:
 * val l = LISAMessage("attribute1"->"a string", "attribute2"-> 1)
 * creates a message with only a body. You can use any number of key-value pairs.
 * 
 * to add header as well, use
 * val l = LISAMessage("attribute1"->"a string", "attribute2"-> 1) addHeader("h1"->"foo")
 * 
 * To add attributes to a message (creates a new updated message) use
 * val newL = l + ("attr3"->true)
 * 
 * observe that the body has a key of type string and a value of type LISAValue. So the 
 * above use implicit conversion from e.g. string and int to LISAValue. If it does not work
 * use "attr1" -> LISAValue("hej")
 * 
 */
case class LISAMessage(body: Map[String, LISAValue], header: Map[String, Any] = Map()) {
  
  def +(key: String, value: List[LISAValue]): LISAMessage = LISAMessage(body + (key->ListPrimitive(value)), header)
  def +(mapEntry: (String,LISAValue)): LISAMessage = LISAMessage(body + mapEntry, header)
  def +(key: String, value: LISAValue): LISAMessage = LISAMessage(body + (key->value), header)
  def ++(maps: (String, LISAValue)*): LISAMessage = LISAMessage(body ++ maps, header)
  def ++(maps: Map[String, LISAValue]): LISAMessage = LISAMessage(body ++ maps, header)
  def get(attribute: String) = body.get(attribute)
  def getAsString(attribute: String) = extract(body.get(attribute), _.asString)
  def getAsInt(attribute: String) = extract(body.get(attribute), _.asInt)
  def getAsLong(attribute: String) = extract(body.get(attribute), _.asLong)
  def getAsDouble(attribute: String) = extract(body.get(attribute), _.asDouble)
  def getAsBool(attribute: String) = extract(body.get(attribute), _.asBool)
  def getAsList(attribute: String) = extract(body.get(attribute), _.asList)
  def getAsMap(attribute: String) = extract(body.get(attribute), _.asMap)
  def getAsDate(attribute: String) = extract(body.get(attribute), _.asDate)
  def getAsDuration(attribute: String) = extract(body.get(attribute), _.asDuration)
  
  def getHeader(attribute: String) = header.get(attribute)
  def addHeader(k: String, v: Any) = LISAMessage(body, header + (k -> v))
  def getTopic: String = {
    tryWithOption(header("JMSDestination").asInstanceOf[javax.jms.Topic]) match {
      case Some(t) => t.getTopicName()
      case None => ""
    }
  }
  
  /**
   * Helper method that extracts a type from the message. Used by the above 
   * getAs* methods.
   */
  def extract[T](attr: Option[LISAValue], f: LISAValue => Option[T]) = {
    for {
      lv <- attr
      v <- f(lv)
    } yield v
  }
  
  /**
   * Helper method to be used instead of a try - catch
   */
  def tryWithOption[T](t: => T): Option[T] = {
    try {
      Some(t)
    } catch {
      case e: Exception => None
    }
  }
}

sealed abstract class LISAValue {
  def asString: Option[String] = LISAValue.this match {
    case StringPrimitive(s) => Some(s)
    case _ => None
  }
  def asInt: Option[Int] = LISAValue.this match {
    case IntPrimitive(i) => Some(i)
    case LongPrimitive(i) => Some(i.toInt)
    case _ => None
  }
  def asLong: Option[Long] = LISAValue.this match {
    case LongPrimitive(i) => Some(i)
    case _ => None
  }
  def asDouble: Option[Double] = LISAValue.this match {
    case DoublePrimitive(d) => Some(d)
    case IntPrimitive(i) => Some(i.toLong)
    case _ => None
  }
  def asBool: Option[Boolean] = LISAValue.this match {
    case BoolPrimitive(b) => Some(b)
    case _ => None
  }
  def asDate: Option[DateTime] = LISAValue.this match {
    case DatePrimitive(d) => Some(d)
    case _ => None
  }
  def asDuration: Option[Duration] = LISAValue.this match {
    case DurationPrimitive(d) => Some(d)
    case _ => None
  }
  def asList: Option[List[LISAValue]] = LISAValue.this match {
    case ListPrimitive(l) => Some(l)
    case _ => None
  }
  def asMap: Option[Map[String, LISAValue]] = LISAValue.this match {
    case MapPrimitive(m) => Some(m)
    case _ => None
  }
  
  def +(lv: LISAValue) = LISAValue(exp(this) ++ exp(lv))
  
  private def exp(lv: LISAValue): List[LISAValue] = lv match{
    case l: ListPrimitive => l.value
    case _ => List(lv)
  }
}

case class StringPrimitive(value: String) extends LISAValue
case class IntPrimitive(value: Int) extends LISAValue
case class LongPrimitive(value: Long) extends LISAValue
case class DoublePrimitive(value: Double) extends LISAValue
case class BoolPrimitive(value: Boolean) extends LISAValue
case class DatePrimitive(value: DateTime) extends LISAValue 
case class DurationPrimitive(value: Duration) extends LISAValue 
case object NonePrimitive extends LISAValue 
case class ListPrimitive(value: List[LISAValue]) extends LISAValue
case class MapPrimitive(value: Map[String, LISAValue]) extends LISAValue

object LISAMessage {
  def apply(mapEntries: (String, LISAValue)*): LISAMessage = LISAMessage(mapEntries.toMap)  
  def apply(mapEntry: (String, LISAValue)): LISAMessage = LISAMessage(Seq(mapEntry):_*)
  
  /**
   * Helper method used together with getWith* above. If you are sure these methods will return 
   * a value, you can extract the value with this method, 
   * ex: val date = as(message.getAsDate("StartTime"))
   * if StartTime does not exist in the message, an NoSuchElementException will be thrown
   * 
   * Better to use monad pattens with for or map etc.
   */
  def as[T](o: Option[T]): T = {
    o match {
      case Some(x) => x
      case None => throw new NoSuchElementException
    }
  }
}

object LISAValue{
  def apply(x: Any): LISAValue = x match {
    case x: String => StringPrimitive(x)
    case x: Int => IntPrimitive(x)
    case x: Long => LongPrimitive(x)
    case x: Boolean => BoolPrimitive(x)
    case x: DateTime => DatePrimitive(x)
    case x: Duration => DurationPrimitive(x)
    case xs: List[_] => ListPrimitive((xs filter (_.isInstanceOf[LISAValue])).asInstanceOf[List[LISAValue]])
    case x: Option[_] => x match {
    	case Some(y) => LISAValue.apply(y)
    	case None => NonePrimitive
    }
    case xs: Map[_, _] => MapPrimitive((xs filter {
      case (k,v)=> k.isInstanceOf[String] && v.isInstanceOf[LISAValue]}).asInstanceOf[Map[String, LISAValue]])
      
    case x: LISAValue => x
    case a:Any => println(a+a.getClass.toString()); NonePrimitive
  }
  
  implicit def stringToPrimitive(x: String): LISAValue = StringPrimitive(x)
  implicit def intToPrimitive(x: Int): LISAValue = IntPrimitive(x)
  implicit def longToPrimitive(x: Long): LISAValue = LongPrimitive(x)
  implicit def doubleToPrimitive(x: Double): LISAValue = DoublePrimitive(x)
  implicit def boolToPrimitive(x: Boolean): LISAValue = BoolPrimitive(x)
  implicit def dateToPrim(x: org.joda.time.DateTime): LISAValue = DatePrimitive(x)
  implicit def durationToPrim(x: org.joda.time.Duration): LISAValue = DurationPrimitive(x)
}

object DatePrimitive {
  def stringToDate(s: String, pattern: String = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"): Option[LISAValue] = {
     val fmt = org.joda.time.format.DateTimeFormat.forPattern(pattern)
     try
       Some(LISAValue(fmt.parseDateTime(s)))
     catch {
       case e:Exception => None
     }   
  }
  def now = DatePrimitive(DateTime.now)
}
