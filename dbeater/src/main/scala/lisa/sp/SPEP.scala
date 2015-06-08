package lisa.sp

import akka.actor._
import lisa.endpoint.message._
import lisa.endpoint.esb._

case class ReadSettings(start: String)

/**
 * Handles the SP UI an the LISA DEMO
 * This EP reads the scania db from mssql and sends out LISAMessages
 *
 */
object SPEP {
  def props(topics: List[String]) = Props(classOf[SPEP], LISAEndPointProperties("SPEP", List(), topics, _=>false))

}

class SPEP(prop: LISAEndPointProperties) extends LISAEndPoint(prop) {


  def receive = {
    case file: ReadSettings => {
      // read here

      //context.system.shutdown()
    }
  }


}



object DBConnect {


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

  case class MachineEvent(eventid: Int,
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



  object DIDRIK_Machining_DM extends Schema {
    val prodStatusTypes = table[ProdStatusTypes]
    val productEvents = table[ProductEvent]("ProductEvents")

  }

  val databaseConnectionUrl = "jdbc:jtds:sqlserver://localhost/;DatabaseName=DIDRIK_Machining_DM"
  val databaseUsername = "a"
  val databasePassword = "a"

  Class.forName("net.sourceforge.jtds.jdbc.Driver")

    def read = {

      SessionFactory.concreteFactory = Some(()=>
        Session.create(
          java.sql.DriverManager.getConnection(databaseConnectionUrl, databaseUsername, databasePassword),
          new MSSQLServer))

      val first =  Timestamp.valueOf("2013-11-01 00:00:00")
      val last = Timestamp.valueOf("2014-01-01 00:00:00")
      transaction {
        val pst = DIDRIK_Machining_DM.prodStatusTypes.where(c=> c.id === 2).single
        println("pst name: " + pst.text)

        val pe = DIDRIK_Machining_DM.productEvents.where(e=>
          (e.startTime.getOrElse(new Timestamp(0)) gte first) and
          (e.startTime.getOrElse(new Timestamp(0)) lte last)
        )
        println(s"pe name:  + ${pe.head.startTime} ${pe.size}")
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
