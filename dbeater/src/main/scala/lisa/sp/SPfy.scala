package lisa.sp

import akka.actor._
import akka.pattern._
import service._
import akka.util.Timeout
import org.joda.time.DateTime
import lisa.endpoint.message.{StringPrimitive, DatePrimitive}

/**
 * Created by Kristofer on 2014-05-15.
 */
class SPfy extends Actor{
  val elastic = ServiceSettings(context.system).elastic
  import context.dispatcher
  import scala.concurrent.duration._
  implicit val timeout = Timeout(3 seconds)
  val es = context.actorOf(lisa.sp.SPElastic.props(elastic))

  def receive = {
    case p: String => {
      println(s"spfy got $p")
      val sendis = sender
      (es ? GetFromES(p, SPfy.search)).pipeTo(sendis)
    }
    case GetFromTopic(p,s)=> {
      println("spfy get from topic")
      val sendis = sender

      (es ? GetFromES(p, s)).pipeTo(sendis)
    }
    case GetFromRawTopic(p,s)=> {
      println("spfy getraw")
      val sendis = sender
      (es ? GetFromESRaw(p, s)).pipeTo(sendis)
    }
    case GetLeadTimeChart(topic, s) => {
      val sendis = sender
      (es ? GetFromES(topic, s)) map {
        case ESResult(bodies, no) => {
          val rows = for {
            b <- bodies
            pid <- b.getAsString("productID")
            lt <- b.getAsInt("leadtime")
          } yield {
            val value = VF(lt / 60000)
            val name = VF(pid)
            Row(List(name, value))
          }
          val cols = List(Col("n","product", "string"),
                    Col("l","Lead time", "number"))
          val c = Chart(cols, rows)
          println(s"in GetLeadTimeChart, created $c")
          sendis ! c
        }
      }
    }

    case GetLeadTimeDistrChart(topic, s) => {
      val sendis = sender
      (es ? GetFromES(topic, s)) map {
        case ESResult(bodies, no) => {
          val ps = for {
            b <- bodies
            pid <- b.getAsString("productID")
            lt <- b.getAsInt("leadtime")
          } yield ((pid, lt))

          val aggr = ps.foldLeft((0,0,0,0,0,0,0))((b, a) =>{
             a._2 / 1000 match {
               case x if x < 35999 => b.copy(_1 = b._1+1)
               case x if x < 71999 => b.copy(_2 = b._2+1)
               case x if x < 107999 => b.copy(_3 = b._3+1)
               case x if x < 143999 => b.copy(_4 = b._4+1)
               case x if x < 179999 => b.copy(_5 = b._5+1)
               case x if x < 215999 => b.copy(_6 = b._6+1)
               case _ => b.copy(_7 = b._7+1)
             }
          })
          val cols = List(Col("n","Leadtime", "string"),
            Col("l","No of products", "number"))

          val rows = List(
            Row(List(VF("0-9"), VF(aggr._1))),
            Row(List(VF("10-19"), VF(aggr._2))),
            Row(List(VF("20-29"), VF(aggr._3))),
            Row(List(VF("30-39"), VF(aggr._4))),
            Row(List(VF("40-49"), VF(aggr._5))),
            Row(List(VF("50-59"), VF(aggr._6))),
            Row(List(VF("70+"), VF(aggr._7)))
          )

          val c = Chart(cols, rows)
          println(s"in GetLeadTimeDistrChart, created $c")
          sendis ! c

        }
      }
    }
    case GetProductPosChart(topic, pid, s)=> {
      val sendis = sender
      import lisa.endpoint.message._
      (es ? GetFromESID(topic, pid)) map {
        case Some(p: LISAMessage) =>  {
          val seq = p.getAsList("events")
          val posSeqO = seq map(xs=>{
            for {
              x <- xs if x.isInstanceOf[MapPrimitive]
              pos <- (x.asInstanceOf[MapPrimitive]).value.get("position").getOrElse(IntPrimitive(-1)).asInt
              date <- (x.asInstanceOf[MapPrimitive]).value.get("time")
              posInfo <- (x.asInstanceOf[MapPrimitive]).value.get("positionInfo")
              posName <- (posInfo.asInstanceOf[MapPrimitive]).value.get("name").getOrElse(StringPrimitive("unknown")).asString
              dur <- (x.asInstanceOf[MapPrimitive]).value.get("duration").getOrElse(IntPrimitive(0)).asInt
            } yield (s"$pos: $posName", dur)
          })
          println("HELP: "+posSeqO)

          val posSeq = posSeqO match {
            case Some(x)=> x
            case None => List()
          }



//          val ag: (List[LISAMessage], LISAMessage) = (List(), null)
//          val durSeq = posSeq.foldLeft(ag)((b,a)=>{
//            val nA = LISAMessage("position"-> a._1, "time"->a._2)
//            if (b._2 == null){
//              (List(), nA)
//            } else {
//
//              import com.github.nscala_time.time.Imports._
//              val start = b._2.getAsDate("time")
//              val stop = a._2.asDate
//
//              val duration = (for {
//                begin <- start
//                end <- stop
//              } yield begin to end ) match {
//                case Some(x)=>x.toDurationMillis / 1000
//                case None => 0}
//
//              val newMess = b._2 + ("duration" -> duration.toInt)
//              val newL = b._1 :+ newMess
//              (newL, nA)
//            }
//          })

          val rows = for {
            b <- posSeq
          } yield {
            val value = VF(b._2)
            val name = VF(b._1)
            Row(List(name, value))
          }

          val cols = List(Col("n","position", "string"),
            Col("l","duration", "number"))

          val c = Chart(cols, rows)
          println(s"in GetProdPos, created $c")
          sendis ! c
        }
      }
    }

    case GetPositionChart(topic, s) => {
      val sendis = sender
      (es ? GetFromES(topic, s)) map {
        case ESResult(bodies, no) => {
          val ps = for {
            b <- bodies
            id <- b.getAsString("resourceID")
            lt <- b.getAsList("events")
            pi <- b.getAsMap("positionInfo")
            pn <- pi.get("name")
            name <- pn.asString
            filt <-  pi.get("type")
          } yield ((s"$id: $name", lt.size))


          val fold = ps.foldLeft(Map[String, Int]())((b,a)=>{
            if (b.contains(a._1))
              b + (a._1->(b(a._1)+a._2))
            else b + a
          })

          val rows = fold map{case (k,v)=>
            val value = VF(v)
            val name = VF(k)
            Row(List(name, value))
          }

          val cols = List(Col("n","position", "string"),
            Col("l","no of visits", "number"))

          val c = Chart(cols, rows.toList)
          sendis ! c
        }
      }
    }

    case GetResourceUtilChart(topic, s)=>{
      resourceUtil = resourceUtil map{case (k,v)=>{
        resourceState(k) match {
          case "operating"=> k -> v.copy(_1=v._1+1)
          case "idle"=> k -> v.copy(_2=v._2+1)
          case "down"=> k -> v.copy(_3=v._3+1)
        }
      }}
      import scala.util.Random
        val state = modes(Random.nextInt(modes.size))
        val updateMe = machines(Random.nextInt(machines.size))
        resourceState = resourceState + (updateMe->state)

      val rows = resourceUtil map{case (k,v)=>
        val sum = v._1+v._2+v._3
        val op = VF(v._1.toDouble / sum )
        val idle = VF(v._2.toDouble  / sum)
        val down = VF(v._3.toDouble  / sum)
        val name = VF(k)
        Row(List(name, op, idle, down))
      }

      val cols = List(
        Col("n","Machine", "string"),
        Col("o","operating", "number"),
        Col("i","idle", "number"),
        Col("l","down", "number")
      )

      val c = Chart(cols, rows.toList)
      sender ! c


    }

    case AddResourceToUtilChart(machine)=>{
      println("Addresources")
      val m = if (machine.isEmpty) "Machine"+(machines.size+1) else machine
      machines = machines :+ m
      resourceState = resourceState + (m -> "operating")
      resourceUtil = resourceUtil + (m->((1,0,0)))
    }

    case GetResourceState => {
      sender ! resourceState
    }


    case ESResult(bodies, no) => {
      println("got result" + no)
    }
  }

  var resourceUtil = Map(
    "Machine1" -> ((30, 70, 40)),
    "Machine2" -> ((60, 140,60)),
    "Machine3" -> ((40, 40, 50)),
    "Machine4" -> ((80, 10, 30)),
    "Machine5" -> ((100, 20,80))
  )
  var resourceState = Map(
    "Machine1" -> "operating",
    "Machine2" -> "idle",
    "Machine3" -> "operating",
    "Machine4" -> "down",
    "Machine5" -> "idle"
  )
  var machines = List("Machine1", "Machine2", "Machine3", "Machine4", "Machine5")
  val modes = List("operating", "idle", "down")

}

object SPfy {
  def props() = Props(classOf[SPfy])

  val filter = FilterSP(
    Map(),
    DatePrimitive.stringToDate("2013-10-10T19:41:01.000+02:00"),
    DatePrimitive.stringToDate("2013-10-15T19:42:01.000+02:00"))
  val search = SearchSP(Some(filter), None, None)


}


//