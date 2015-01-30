package lisa.endpoint.message

import org.scalatest._

class MessageTest extends FlatSpec with Matchers{
  
  "A LISAValue" should "box int and return it" in {
    val l: LISAValue = 1
    
    l.asInt should be (Some(1))
    l.asString should be (None)
    l.asDouble should be (None)
    l.asBool should be (None)
  }
  
  it should "box string and return it" in {
    val l: LISAValue = "hej"
    
    l.asInt should be (None)
    l.asString should be (Some("hej"))
    l.asDouble should be (None)
    l.asBool should be (None)
  }
  
  it should "box double and return it" in {
    val l: LISAValue = 1.1
    
    l.asInt should be (None)
    l.asString should be (None)
    l.asDouble should be (Some(1.1))
    l.asBool should be (None)
  }
  
  it should "box boolean and return it" in {
    val l: LISAValue = false
    
    l.asInt should be (None)
    l.asString should be (None)
    l.asDouble should be (None)
    l.asBool should be (Some(false))
  }
   
  
  it should "box joda datetime and return it" in {
    import com.github.nscala_time.time.Imports._    
    val t: DateTime = DateTime.now;
    val l: LISAValue = t
    
    l.asInt should be (None)
    l.asString should be (None)
    l.asDouble should be (None)
    l.asBool should be (None)
    l.asDate should be (Some(t))
  }
  
    it should "convert datetime String" in {
    import com.github.nscala_time.time.Imports._    
    val l = DatePrimitive.stringToDate("2013-12-05T13:58:29.095+01:00")
    
    l should not be empty
  }
  
  
}
