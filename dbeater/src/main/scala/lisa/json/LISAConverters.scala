package lisa.json

import spray.json._
import lisa.endpoint.message._
import lisa.sp.ESResult

/**
 * Created by Kristofer on 2014-05-15.
 *
 * Move this to a separate library including elasticsearch API
 *
 */
object LISAConverters extends DefaultJsonProtocol with spray.httpx.SprayJsonSupport {

  implicit object LISAValueFormat extends RootJsonFormat[LISAValue] {
    def write(x: LISAValue) = {
      x match {
        case StringPrimitive(x) => x.toJson
        case IntPrimitive(x) => x.toJson
        case LongPrimitive(x) => x.toJson
        case DoublePrimitive(x) => x.toJson
        case BoolPrimitive(x) => x.toJson
        case DatePrimitive(x) => x.toString().toJson
        case DurationPrimitive(x) => x.getMillis().toJson
        case NonePrimitive => JsNull
        case ListPrimitive(x) => x.toJson
        case MapPrimitive(x) => x.toJson

      }
    }
    def read(value: JsValue) = value match {
      case JsString(x) => {
        DatePrimitive.stringToDate(x) match {
          case Some(d) => d
          case None => StringPrimitive(x)
        }
      }
      case JsNumber(x) => {
        if (x.isValidInt) IntPrimitive(x.intValue)
        else if (x.isValidDouble) DoublePrimitive(x.doubleValue)
        else if (x.isValidLong) LongPrimitive(x.longValue)
        else StringPrimitive(x.toString)
      }
      case JsBoolean(x) => BoolPrimitive(x)
      case JsArray(xs) => ListPrimitive(xs map (_.convertTo[LISAValue]))
      case kvs: JsObject => MapPrimitive(convJsObj(kvs))
      case JsNull => NonePrimitive
      case _ => throw new DeserializationException("can not convert to SPAttributeValue: "+value)
    }
  }

  implicit object LISAMessageFormat extends RootJsonFormat[LISAMessage] {
    def write(x: LISAMessage) = {
      JsObject(
        "body" -> JsObject(x.body map {case (k,v)=> k -> {v.toJson}}),
        "header" -> JsObject(x.header map {case (k,v)=> k -> {v.toString.toJson}})
      )
    }
    def read(value: JsValue) = value match {
      case a: JsObject => {
        val bf = a.fields.get("body")
        val hf = a.fields.get("header")
        val body: Map[String, LISAValue] = bf match {
          case Some(o: JsObject) => {
            convJsObj(o)
          }
          case _ => convJsObj(a)
        }
        val header: Map[String, LISAValue] = hf match {
          case Some(o: JsObject) => {
            convJsObj(o)
          }
          case _ => Map()
        }

        LISAMessage(body, header)
      }
      case _ => throw new DeserializationException("can not convert to LISAMessage: "+value)

    }}

  private def convJsObj(o: JsObject) = o.fields map {case (k,v)=> k->v.convertTo[LISAValue]}

  import lisa.sp._

  implicit val eSResultF = jsonFormat2(ESResult)


  implicit val paginationSPF = jsonFormat2(PaginationSP)
  implicit val sortSPF = jsonFormat2(SortSP)
  implicit val filterSPF = jsonFormat3(FilterSP)
  implicit val searchSPF = jsonFormat3(SearchSP)
  implicit val getFromTopicF = jsonFormat2(GetFromTopic)
  implicit val vfF = jsonFormat2(VF)
  implicit val rowF = jsonFormat1(Row)
  implicit val colF = jsonFormat3(Col)
  implicit val chartF = jsonFormat2(Chart)





  /*  case class Chart(cols: List[Col], rows: List[Row])
    case class Col(id: String, label: String, `type`: String)
    case class Row(c: List[VF])
    case class VF(v: LISAValue, f: Option[String] = None)*/


}

/*
object LISAjson4sConverter {
  import org.json4s._
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization.{read, write}

  implicit val formats = Serialization.formats(NoTypeHints) + new DatePrimitiveSerializer +
    new DurationPrimitiveSerializer +
    new LongPrimitiveSerializer +
    new StringPrimitiveSerializer +
    new IntPrimitiveSerializer +
    new DoublePrimitiveSerializer +
    new BoolPrimitiveSerializer +
    new MapPrimitiveSerializer +
    new ListPrimitiveSerializer

  class StringPrimitiveSerializer extends CustomSerializer[StringPrimitive](format => (
    {
      case JString(s) =>
        new StringPrimitive(s)
    },
    {
      case x: StringPrimitive =>
        JString(x.value)
    }))

  class IntPrimitiveSerializer extends CustomSerializer[IntPrimitive](format => (
    {
      case JInt(x) =>
        new IntPrimitive(x.toInt)
    },
    {
      case x: IntPrimitive =>
        JInt(x.value)
    }))

  class LongPrimitiveSerializer extends CustomSerializer[LongPrimitive](format => (
    {
      case JInt(x) =>
        new LongPrimitive(x.toLong)
    },
    {
      case x: LongPrimitive =>
        JInt(x.value)
    }))

  class DoublePrimitiveSerializer extends CustomSerializer[DoublePrimitive](format => (
    {
      case JDouble(x) =>
        new DoublePrimitive(x)
      case JDecimal(x) => new DoublePrimitive(x.toDouble)
    },
    {
      case x: DoublePrimitive =>
        JDouble(x.value)
    }))

  class BoolPrimitiveSerializer extends CustomSerializer[BoolPrimitive](format => (
    {
      case JBool(x) =>
        new BoolPrimitive(x)
    },
    {
      case x: BoolPrimitive =>
        JBool(x.value)
    }))

  class DatePrimitiveSerializer extends CustomSerializer[DatePrimitive](format => (
    {
      case JString(s) if !DatePrimitive.stringToDate(s).isEmpty  => {
        DatePrimitive.stringToDate(s) match {
          case Some(d: DatePrimitive) => d
          case None => DatePrimitive.now
        }
      }
    },
    {
      case x: DatePrimitive =>
        JString(x.value.toString())
    }))

  class DurationPrimitiveSerializer extends CustomSerializer[DurationPrimitive](format => (
    {
      case JNothing =>
        import com.github.nscala_time.time.Imports._
        DurationPrimitive(1.seconds)
    },
    {
      case x: DurationPrimitive =>
        JInt(x.value.getMillis())
    }))

  class MapPrimitiveSerializer extends CustomSerializer[MapPrimitive](format => (
    {
      case JObject(fs) =>
        MapPrimitive(Map.empty)
    },
    {
      case x: MapPrimitive =>
        val l = x.value map {case (k,v)=>
          JField(k,Extraction.decompose(v))
        }
        JObject(l toList)
    }))


  class ListPrimitiveSerializer extends CustomSerializer[ListPrimitive](format => (
    {
      case JNothing =>
        ListPrimitive(List.empty)
    },
    {
      case x: ListPrimitive =>
        val l = x.value map {v =>
          Extraction.decompose(v)
        }
        JArray(l toList)
    }))



}*/
