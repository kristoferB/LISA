package lisa.logeaterep

object test {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  
  import lisa.endpoint.message._
  import lisa.endpoint.esb._
  
  val time = "2013-10-01 19:20:22.000"            //> time  : String = 2013-10-01 19:20:22.000
  val time2 = "2013-09-11T08:37:10.312Z"          //> time2  : String = 2013-09-11T08:37:10.312Z
  
  
  
  val pattern = "yyyy-MM-dd HH:mm:ss.SSS";        //> pattern  : String = yyyy-MM-dd HH:mm:ss.SSS
  var pattern2 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"   //> pattern2  : String = yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
  
  val fmt  = org.joda.time.format.DateTimeFormat.forPattern(pattern)
                                                  //> fmt  : org.joda.time.format.DateTimeFormatter = org.joda.time.format.DateTim
                                                  //| eFormatter@6e821522
  val fmt2  = org.joda.time.format.DateTimeFormat.forPattern(pattern2)
                                                  //> fmt2  : org.joda.time.format.DateTimeFormatter = org.joda.time.format.DateTi
                                                  //| meFormatter@1e39a3dc
  
  
  //val fmt = org.joda.time.format.ISODateTimeFormat.dateTime()
  
  fmt.parseDateTime(time)                         //> res0: org.joda.time.DateTime = 2013-10-01T19:20:22.000+02:00
  fmt2.parseDateTime(time2)                       //> res1: org.joda.time.DateTime = 2013-09-11T08:37:10.312+02:00


  val t = DatePrimitive.stringToDate(time)        //> t  : Option[lisa.endpoint.message.LISAValue] = None
  val t2 = DatePrimitive.stringToDate(time2)      //> t2  : Option[lisa.endpoint.message.LISAValue] = Some(DatePrimitive(2013-09-1
                                                  //| 1T10:37:10.312+02:00))
  
  
}