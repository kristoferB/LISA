package lisa.endpoint.esb

import lisa.endpoint.message._

case class LISAEndPointProperties(
    endpointName : String,
    topics : List[String],
    messageFilter: LISAMessage => Boolean = (_ => true)
)