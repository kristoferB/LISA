package lisa.elasticsearch

import akka.actor._
import akka.camel._
import lisa.endpoint.message._
import lisa.endpoint.esb._


/**
 * Takes operations and fold them based on productid.
 *
 */
class ElasticSearchEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  
  import com.sksamuel.elastic4s.ElasticClient
  import com.sksamuel.elastic4s.ElasticDsl._
  val client = ElasticClient.remote("192.168.89.133", 9300)

  var count = 0
  
  prop.topics foreach { topic =>
    client execute { create index topic.toLowerCase() }
  }

  
  def receive = {
    case mess: LISAMessage => {
    	sendToES(mess)
    }
    case mess : CamelMessage => {
      sendToES(LISAMessage(parseJson(mess.body),mess.headers))    
    }
  }
  
  
  def sendToES(mess: LISAMessage) = {
    val topic = mess.getTopic.toLowerCase()
    import com.sksamuel.elastic4s.ElasticDsl._
    client.execute { index into topic + "/lisamessage" doc Convert(mess) }
    /*client.execute {
      val temp = index into topic fields mess.body
      temp.
	//      val temp = index into "lisamessage" doc mess
	//
	//      index into "lisamessage" doc mess
  	}*/
    //println(topic+" no:"+count)
    //count += 1
    //log.debug("Sending to ES on : "+topic+" mess:" + mess)
   }
  
  def parseJson(body: Any) : Map[String,LISAValue] = {
    val messbody = body.asInstanceOf[String].filterNot("\n\r{}".toSet)
    var map : Map[String,LISAValue] = Map.empty
    val splitArray = messbody.split(",")
    for(element <- splitArray) {
      val lineSplit = element.split("\"")
      map = map + (lineSplit(1) -> LISAValue(lineSplit(3)))    
    }
      map    
  }
  
  
}

object ElasticSearchEP {
  def props(topics: List[String]) = Props(classOf[ElasticSearchEP], LISAEndPointProperties("MessageConsumerTest", topics))

}


 import com.sksamuel.elastic4s.source._
  case class Convert(lm: LISAMessage) extends DocumentSource  {
    import org.json4s._
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.{ read, write }
    implicit val formats = Serialization.formats(NoTypeHints) + 
    		new StringPrimitiveSerializer + 
    		new IntPrimitiveSerializer + 
    		new DoublePrimitiveSerializer + 
    		new BoolPrimitiveSerializer +
    		new DatePrimitiveSerializer +
    		new DurationPrimitiveSerializer +
    		new MapPrimitiveSerializer +
    		new ListPrimitiveSerializer
    
    val json: String = {
      write(lm)
    }

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
 
     class DoublePrimitiveSerializer extends CustomSerializer[DoublePrimitive](format => (
      {
        case JDouble(x) =>
          new DoublePrimitive(x)
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
        case JNothing  =>
          DatePrimitive.now
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
        case JNothing =>
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
  }

