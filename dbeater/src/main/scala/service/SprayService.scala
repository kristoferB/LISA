package service

import akka.actor._
import akka.pattern.ask
import spray.http.MediaTypes._
import spray.routing._
import org.joda.time.DateTime
import akka.util.Timeout
import lisa.sp._
import lisa.endpoint.message._
import lisa.json._
import scala.concurrent.ExecutionContext


/**
 * Spray service
 *   - a REST under `spray-json-message/`
 *   - a HTML under `spray-html/`
 */
trait SprayService extends HttpService {
  val sp: ActorRef

  import scala.concurrent.duration._
  implicit val timeout = Timeout(3 seconds)

  import reflect.ClassTag
  import ExecutionContext.Implicits.global
  import spray.httpx.unmarshalling._
  import spray.httpx.marshalling._

  import LISAConverters._
  import spray.util._



  def adRoute : Route =
    path("raw-events" / Segment) { topic =>
      get {
       // val mess = GetFromTopic(topic, search)
        //complete((sp ? mess).mapTo[ESResult])
        complete("")
      }
    } ~
    path("spray-html") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                <h1>Hello papa!</h1>
              </body>
            </html>
          }
        }
      }
    }



}
