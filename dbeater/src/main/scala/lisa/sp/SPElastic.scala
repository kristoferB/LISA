//package lisa.sp
//
//import akka.actor._
//import lisa.endpoint.message.LISAMessage
//import spray.http.HttpResponse
//
//case class GetFromES(topic: String, search: SearchSP)
//case class GetFromESRAwWithQ(topic: String, q: String)
//case class GetFromESRaw(topic: String, search: SearchSP)
//case class GetFromESID(topic: String, id: String)
//case class ESResult(res: List[LISAMessage], no: Int = -1)
//
//
//object SPElastic {
//  def props(address: String) = Props(classOf[SPElastic], address)
//}
//
//class SPElastic(address: String) extends Actor {
//
//  import lisa.endpoint.message._
//  import spray.json._
//  import DefaultJsonProtocol._
//  import lisa.json.LISAConverters._
//  import context.dispatcher
//
//
//
//  def receive = {
//    case GetFromESRaw(topic, s) => {
//      val sendis = sender
//      val q = makeQ(s)
//      println(q)
//      val cl = new ESClient(address, context.system)
//      cl.searchGetBody(topic, q.parseJson).onSuccess({
//        case res: JsObject => {
//          sendis ! res
//        }
//        case _ => println("Error in ES search SPElastic")
//      })
//    }
//
//    // Returns a list of jsons
//    case GetFromES(topic, search) => {
//      val cl = new ESClient(address, context.system)
//      val q = makeQ(search)
//      println(q)
//      val sendis = sender
//      cl.searchGetBody(topic, q.parseJson).onSuccess({
//        case res: JsObject => {
//          val bodies = extractTheBodies(res)
//          val no = getJs[JsNumber](res, List("hits", "total"))
//          sendis ! makeRes(bodies, no)
//        }
//        case _ => println("Error in ES search SPElastic")
//      })
//
//    }
//
//    case GetFromESID(topic, id) => {
//      val cl = new ESClient(address, context.system)
//      val sendis = sender
//      cl.get(topic, "lisamessage", id).onSuccess({
//        case res: HttpResponse => {
//          val json = res.entity.asString.parseJson
//          println("prodID: "+json)
//          val bodyOp = getJs[JsObject](json, List("_source", "body"))
//          val mess = bodyOp map (body => body.convertTo[LISAMessage])
//          sendis ! mess
//        }
//      })
//    }
//  }
//
//
//  def makeQ(search: SearchSP) = {
//    val rangeQO = for {
//      filter <- search.filter
//      x <- filter.startDate
//      start <- x.asDate
//      y <- filter.stopDate
//      stop <- y.asDate
//    } yield {
//      """"range" : {"starttime" : {"gte" : """" + start +
//        """","lte" : """" + stop +
//        """"}}"""
//    }
//
//    val pagination = for {
//      pag <- search.pagination
//    } yield {
//      """ "from":""" + pag.start + """, "size": """ + pag.no
//    }
//
//    val rangeQ = rangeQO match {
//      case Some(x) => x
//      case None => ""
//    }
//    val pagQ = pagination match {
//      case Some(x) => x
//      case None => """ "from":""" + 0 + """, "size": """ + 10
//    }
//
//    """{"query": {""" + rangeQ + "},"+ pagQ +"}"
//  }
//
//  def getJs[T](json: JsValue, levels: List[String]): Option[T] = {
//    try {
//      levels match {
//        case Nil => Some(json.asInstanceOf[T])
//        case x :: xs => {
//          getJs[T](json.asJsObject.fields(x), xs)
//        }
//      }
//    } catch {
//      case e: DeserializationException => {
//        None
//      }
//      case e: NoSuchElementException => None
//    }
//  }
//
//  def getJs[T](json: JsValue, key: String): Option[T] = getJs(json, List(key))
//
//  def extractTheBodies(mess: JsObject) = {
//    val hits = getJs[JsArray](mess, List("hits", "hits"))
//    val result = hits map (_.elements map (h => {
//      val bodyOp = getJs[JsObject](h, List("_source", "body"))
//      bodyOp map (body => body.convertTo[LISAMessage])
//    }))
//
//    result map (xs => for {
//      x <- xs
//      v <- x
//    } yield v)
//  }
//
//  def makeRes(res: Option[List[LISAMessage]], total: Option[JsNumber]): ESResult = {
//    val k = res match {
//      case Some(x) => x
//      case None => List[LISAMessage]()
//    }
//    val no = total match {
//      case Some(n) => n.value.toInt
//      case None => -1
//    }
//
//    ESResult(k, no)
//  }
//
//}
//
///*
//{
//    "query": {
//        "range" : {
//        "starttime" : {
//            "gte" : "2013-10-10T19:41:01.000+02:00",
//            "lte" : "2013-10-15T19:42:01.000+02:00"
//        }
//    }
//    }
//}
//
//
//cl.searchGetBody("productfold", q).onSuccess({
//          case res: JsObject => {
//            val hits = getJs[JsArray](res, List("hits", "hits"))
//
//            import se.sekvensa.sp.runtime.domain._
//            import se.sekvensa.sp.com.server.JsonConverters._
//
//
//            val result = hits map (_.elements map (h=>{
//              val bodyOp = getJs[JsObject](h, List("_source", "body"))
//              bodyOp map (body => {
//                val name = getJs[JsString](body, "productID") map (x=>SPAttributeValue(x.value))
//                val events = getJs[JsArray](body, "events") map(_.elements map (e => {
//                  val pos = SPAttributeValue(getJs[JsNumber](e,"position") map (x=>SPAttributeValue(x.value.toInt)))
//                  val time = SPAttributeValue(getJs[JsString](e,"time") map (t =>DatePrimitive.stringToDate(t.value)))
//                  val id = SPAttributeValue(getJs[JsString](e,"id") map (x=>SPAttributeValue(x.value)))
//                  SPAttributeValue(Map("id"->id, "position"->pos, "time"->time))
//
//                }))
//
//                val attrs: Map[String, SPAttributeValue] = Map(
//                		"ProductID" -> SPAttributeValue(name),
//                		"LisaID" -> SPAttributeValue(getJs[JsString](body, "lisaID") map (x=>SPAttributeValue(x.value))),
//                		"StartTime" -> SPAttributeValue(getJs[JsString](body, "starttime") map(d=>DatePrimitive.stringToDate(d.value))),
//                		"StopTime" -> SPAttributeValue(getJs[JsString](body, "stoptime") map(d=>DatePrimitive.stringToDate(d.value))),
//                		"LeadTime" -> SPAttributeValue(getJs[JsNumber](body, "leadtime") map (x=>SPAttributeValue(x.value.longValue))),
//                		"events" -> SPAttributeValue(events map (es=> SPAttributeValue(es)))
//                )
//
//                import se.sekvensa.sp.model.domain._
//                val entity = for {
//                  temp <- name
//                  ename <- temp.asString
//                  lisa <- getJs[JsString](body, "lisaID")
//                } yield {
//                  new Entity(ename, Set(), SPAttributes(attrs)){
//                    	override val id = ID(lisa.value)
//                    }
//                }
//
//                entity
//
//              })
//
//            }))
//
//            result map (_ flatMap(_.flatMap(_.map(f=>f)))) map (res => replyTo ! res)
//
//          }
//        })
//
//
// */
//
