//package service
//
//import spray.routing._
//import akka.actor._
//import akka.pattern.ask
//import akka.util._
//import scala.concurrent.duration._
//import lisa.sp._
//import lisa.endpoint.message._
///*import spray.httpx.unmarshalling._
//import spray.httpx.marshalling._*/
//
//
//
///**
// * Created by Kristofer on 2014-05-16.
// */
//class GUIRouter(sp: ActorRef) extends HttpServiceActor {
//  override def receive: Receive =
//    runRoute(myRoute ~ staticRoute)
//
//
//  import context.dispatcher
//
///*  import reflect.ClassTag
//  import lisa.json.LISAConverters._
//  import MyJsonProtocol._
//  import spray.httpx.SprayJsonSupport._
//  import spray.json._
//  import DefaultJsonProtocol._*/
//
//
//  import spray.json._
//  import lisa.json.LISAConverters._
//
//  implicit val timeout = Timeout(3 seconds)
//  def myRoute: Route = {
//    path("raw-events" / Segment) { topic =>
//      get {
//        complete((sp ? topic).mapTo[ESResult])
//      } ~
//        post {
//          entity(as[SearchSP]) { search =>
//            complete((sp ? GetFromRawTopic(topic, search)).mapTo[JsObject])
//          }
//        }
//    } ~
//    path("events" / Segment) { topic =>
//      get {
//        complete((sp ? topic).mapTo[ESResult])
//      } ~
//        post {
//          entity(as[SearchSP]) { search =>
//            complete((sp ? GetFromTopic(topic, search)).mapTo[ESResult])
//          }
//        }
//    } ~
//    path("chart" / "leadtime" / Segment) { topic =>
//        post {
//          entity(as[SearchSP]) { search =>
//            complete((sp ? GetLeadTimeChart(topic, search)).mapTo[Chart])
//          }
//        }
//    } ~
//      path("chart" / "leadtimedistr" / Segment) { topic =>
//        post {
//          entity(as[SearchSP]) { search =>
//            complete((sp ? GetLeadTimeDistrChart(topic, search)).mapTo[Chart])
//          }
//        }
//      }~
//      path("chart" / "productpos" / Segment) { pid =>
//        post {
//          entity(as[SearchSP]) { search =>
//            complete((sp ? GetProductPosChart("productfold2", pid, search)).mapTo[Chart])
//          }
//        }
//      }~
//      path("chart" / "positions" / Segment) { topic =>
//        post {
//          entity(as[SearchSP]) { search =>
//            complete((sp ? GetPositionChart(topic, search)).mapTo[Chart])
//          }
//        }
//      } ~
//      path("chart" / "resourceutil" ) {
//            complete((sp ? GetResourceUtilChart(null, null)).mapTo[Chart])
//      } ~
//      path("chart" / "resourcestate" ) {
//            complete((sp ? GetResourceState).mapTo[Map[String, String]])
//      }~
//      path("chart" / "newmachine" ) {
//        complete {
//          sp ! AddResourceToUtilChart("")
//          "ok"
//        }
//      }
//
//
//
//
///*    path("test") {
//      val t1 = "hej".toJson
//      val t2 = LISAMessage("hej"-> LISAValue(1)).toJson
//      //complete(LISAMessage("hej"-> LISAValue(1)))
//      complete((sp ? "hej").mapTo[ESResult])
///*      (sp?"hej").onSuccess{
//        case l: LISAMessage => {
//          complete(l)
//        }
//      }*/
//
///*      (sp?"hej").onSuccess{
//        case x: Person2 => {
//
//
//          complete({
//            import spray.json._
//            import DefaultJsonProtocol._
//            import MyJsonProtocol._
//            import spray.httpx.SprayJsonSupport._
//            Map("hej"->"hej").toJson.asJsObject
//          })
//        }
//      }*/
//
//      complete("")
//    }*/
//  }
//
//
//  def staticRoute: Route = {
//    //path("")(getFromResource("webapp/index.html")) ~ getFromResourceDirectory("webapp")
//    path("")(getFromFile("src/main/webapp/index.html")) ~ getFromDirectory("src/main/webapp")
//  }
//}
//
//object GUIRouter {
//  def props(routeToMe: ActorRef) = Props(classOf[GUIRouter],routeToMe)
//
//  val filter = FilterSP(
//    Map(),
//    DatePrimitive.stringToDate("2013-10-10T19:41:01.000+02:00"),
//    DatePrimitive.stringToDate("2013-10-15T19:42:01.000+02:00"))
//  val search = SearchSP(Some(filter), None, None)
//}
//
