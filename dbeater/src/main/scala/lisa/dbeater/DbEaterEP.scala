package lisa.dbeater

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._
import java.sql.Timestamp
import org.joda.time.DateTime

case class ReadSettings(start: String)

/**
 * This EP reads the scania db from mssql and sends out LISAMessages
 *
 */
class DbEaterEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {
  val rawTopic = prop.topics(0)
  val filledTopic = prop.topics(1)

  def receive = {
    case "go" => {
      val px = DB.getProductEvents(Timestamp.valueOf("2013-10-01 00:00:00"), Timestamp.valueOf("2014-01-31 00:00:00"))
      val pos = DB.getPositionInfo()

      val posMap = (pos map(p=>{
        p.positionID -> p
      })) toMap

      val smallPE = px
      val PEMess = smallPE map(pe=>{
        val m = Map("eventID" -> LISAValue(pe.eventid.toString),
          "productID" -> LISAValue(pe.productID.getOrElse("").trim().toUpperCase()),
          "position" -> LISAValue(pe.position),
          "starttime" -> cd(pe.startTime),
          "stoptime" -> cd(pe.finishTime),
          "duration" -> LISAValue(pe.durationInSeconds),
          "lisaID" -> LISAValue(pe.eventid.toString),
          "operationType" -> LISAValue({if (pe.finishTime.isEmpty) "merge" else "transport"})
        )

        val posObj = for {
          p <- pe.position
          pObj <- posMap.get(p)
        } yield {
          MapPrimitive(Map( "name"->LISAValue(pObj.name), "type"->LISAValue({if (pObj.positionType==1) "machine" else "transport"})))
        }

        (LISAMessage(m), LISAMessage(m+("positionInfo"->LISAValue(posObj))))
      })

      PEMess foreach(m=>{
        sendTo(rawTopic) ! m._1
        sendTo(filledTopic) ! m._2
      })
    }
  }

  def cd(t: Option[Timestamp]): LISAValue = {
    import org.joda.time.DateTime
    t match {
      case Some(x)=> new DateTime(x)
      case None => NonePrimitive
    }
  }
}

object DBEaterEP {
  def props(rawTopic: String, filledTopic: String) = Props(classOf[DbEaterEP], LISAEndPointProperties("dbEater", List(rawTopic, filledTopic), _=>false))
}

object DB {

  import org.squeryl.PrimitiveTypeMode._
  import org.squeryl.Schema
  import org.squeryl.annotations.Column
  import java.util.Date
  import java.sql.Timestamp
  import org.squeryl.Session
  import org.squeryl.adapters.MSSQLServer
  import org.squeryl.SessionFactory
  import org.squeryl.PrimitiveTypeMode._
  import org.squeryl.Schema

  case class ProdStatusTypes(id: Int,
                        text: Option[String],
                        description: Option[String])

  case class ProductEvent(eventid: Int,
                      productID: Option[String],
                      position: Option[Int],
                      startTime: Option[Timestamp],
                      finishTime: Option[Timestamp],
                      durationInSeconds: Option[Int]) {

    def this() = this(
      0,
      Some(""),
      Some(0),
      Some(new Timestamp(0)),
      Some(new Timestamp(0)),
      Some(0)
    )

  }

  case class MachineEvent(id: Int,
                          machineID: Option[String],
                          startTime: Option[Timestamp],
                          finishTime: Option[Timestamp],
                          durationInSeconds: Option[Int]) {

    def this() = this(
      0,
      Some(""),
      Some(new Timestamp(0)),
      Some(new Timestamp(0)),
      Some(0)
    )

  }

  case class Machine(machineID: String,
                      name: Option[String],
                      position: Option[Int],
                      make: Option[String],
                      description: Option[String]){
    def this() = this(
      "",
      Some(""),
      Some(0),
      Some(""),
      Some("")
    )
  }

  case class Position(positionID: Int,
                      name: Option[String],
                      positionType: Int){

    def this() = this(
      0,
      Some(""),
      0
    )
  }



  object DIDRIK_Machining_DM extends Schema {
    val prodStatusTypes = table[ProdStatusTypes]
    val productEvents = table[ProductEvent]("ProductEvents")
    val machineEvents = table[MachineEvent]("MachineEvents")
    val machines = table[Machine]("Machines")
    val position = table[Position]("Positions")

  }

  val databaseConnectionUrl = "jdbc:jtds:sqlserver://localhost/;DatabaseName=DIDRIK_Machining_DM"
  val databaseUsername = "a"
  val databasePassword = "a"

  Class.forName("net.sourceforge.jtds.jdbc.Driver")

  SessionFactory.concreteFactory = Some(()=>
    Session.create(
      java.sql.DriverManager.getConnection(databaseConnectionUrl, databaseUsername, databasePassword),
      new MSSQLServer))

  def getProductEvents(startTime: Timestamp, stopTime: Timestamp) = {
      var res = List[ProductEvent]()
      transaction{
        val q = DIDRIK_Machining_DM.productEvents.where(e=>
          (e.startTime.getOrElse(new Timestamp(0)) gte startTime) and
            (e.startTime.getOrElse(new Timestamp(0)) lte stopTime)
        )
        res = q.toList
      }

      res
  }

  def getPositionInfo() = {
    var res = List[Position]()
    transaction{
      val q = DIDRIK_Machining_DM.position
      res = q.toList
    }
    res
  }

    def read = {

      SessionFactory.concreteFactory = Some(()=>
        Session.create(
          java.sql.DriverManager.getConnection(databaseConnectionUrl, databaseUsername, databasePassword),
          new MSSQLServer))

      val first =  Timestamp.valueOf("2013-10-01 00:00:00")
      val last = Timestamp.valueOf("2014-01-31 00:00:00")
      transaction {
        val pst = DIDRIK_Machining_DM.prodStatusTypes.where(c=> c.id === 2).single

        val pe = DIDRIK_Machining_DM.productEvents.where(e=>
          (e.startTime.getOrElse(new Timestamp(0)) gte first) and
          (e.startTime.getOrElse(new Timestamp(0)) lte last)
        )

/*        val me = DIDRIK_Machining_DM.machineEvents.where(e=>
          (e.startTime.getOrElse(new Timestamp(0)) gte first) and
            (e.startTime.getOrElse(new Timestamp(0)) lte last)
        )*/

        val positions = DIDRIK_Machining_DM.position

        println(s"prodevents: ${pe.size}")
        println(s"positions: ${positions.size}")

        val product = pe.head
        val prodPos = for {
          posid <- product.position
          p <- positions.find(_.positionID == posid)
        } yield p

        println(s"the product $product has position $prodPos")
      }

/*    DatabaseUtils.database withSession {
      implicit session => {
        prodStatusTypes foreach { case (id, text, desc) =>
          println("  " + id + "\t" + text + "\t" + desc)
        }
      }
    }*/

/*    Database.forURL(
      "jdbc:jtds:sqlserver://localhost:1433/DIDRIK_Machining_DM;instance=SQLEXPRESS",
      driver = "scala.slick.driver.SQLServerDriver") withSession {
      implicit session => {
        prodStatusTypes foreach { case (id, text, desc) =>
          println("  " + id + "\t" + text + "\t" + desc)
        }
       }
      }*/

  }



}
