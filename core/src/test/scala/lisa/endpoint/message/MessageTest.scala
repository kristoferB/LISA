package lisa.endpoint.message

import java.util.Date

import org.scalatest._
import org.json4s._
//import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL.WithDouble._
import org.json4s.native.Serialization.{read, write}

trait Kalle
case class SUBTest(key3: Int, key4: Boolean) extends Kalle
case class OPTest(key1: String, key2: String, sub1: SUBTest) extends Kalle
case class ADate(date: org.joda.time.DateTime)

class MessageTest extends FlatSpec with Matchers{
  import MessageLogic._

  implicit val f =  new LISAFormats {
    override val typeHints = ShortTypeHints(List(classOf[SUBTest], classOf[OPTest]))
  }

  val d = org.joda.time.DateTime.now();
  val dateString = write(d).replace("\"", "")

  val uuid = java.util.UUID.randomUUID()
  val uuidString = write(uuid).replace("\"", "")

  val json =
    ("op1" ->
      ("key1" -> "hej") ~
        ("key2" -> "hej") ~
        ("sub1" ->
          ("key3" -> 1) ~
            ("key4" -> false)
          )
      )  ~
      ("op2" ->
        ("key1" -> "då") ~
          ("key2" -> "då") ~
          ("sub1" ->
            ("key3" -> 2) ~
              ("key4" -> true)
            )
        ) ~
      ("string" -> "Det gick") ~
      ("int" -> 1) ~
      ("boolean" -> false) ~
      ("double" -> 1.0) ~
      ("date" -> dateString) ~
      ("uuid" -> uuidString)

  val mess = LISAMessage(body = json)

//  val key1 = mess.find("int")
//  val sub = mess.find("sub1")
//  println(s"sub1: $sub")
//  println(s"key1: $key1")
//  val asString = mess.getAs[OPTest]("op1")
//  println(s"got it: $asString")


  "A LISAMessage" should "get as string" in {
    mess.getAs[String]("string") shouldEqual Some("Det gick")
  }
  "A LISAMessage" should "return none if no key" in {
    mess.getAs[String]("nejNej") shouldEqual None
  }
  "A LISAMessage" should "return none if not correct type" in {
    mess.getAs[String]("boolean") shouldEqual None
  }
  "A LISAMessage" should "get as int" in {
    mess.getAs[Int]("int") shouldEqual Some(1)
  }
  "A LISAMessage" should "get as boolean" in {
    mess.getAs[Boolean]("boolean") shouldEqual Some(false)
  }
  "A LISAMessage" should "get as double" in {
    mess.getAs[Double]("double") shouldEqual Some(1.0)
  }
  "A LISAMessage" should "get as datetime" in {
    mess.getAs[org.joda.time.DateTime]("date") shouldEqual Some(d)
  }
  "A LISAMessage" should "get as UUID" in {
    mess.getAs[java.util.UUID]("uuid") shouldEqual Some(uuid)
  }
  "A LISAMessage" should "find all values" in {
    val res = mess.find("key1")
    res shouldEqual List(JString("hej"), JString("då"))
  }
  "A LISAMessage" should "find all objects as" in {
    val res = mess.findAs[SUBTest]("sub1")
    res shouldEqual List(SUBTest(1, false), SUBTest(2, true))
  }
  "A LISAMessage" should "find simple primitive as" in {
    val res = mess.findAs[String]("string")
    res shouldEqual List("Det gick")
  }
  "A LISAMessage" should "find objects with keys" in {
    val res = mess.findObjectsWithKeys(List("key3", "key4"))
    res shouldEqual List(
      ("sub1",JObject(
        List(("key3",JInt(1)), ("key4",JBool(false))))),
      ("sub1",JObject(
        List(("key3",JInt(2)), ("key4",JBool(true))))))
  }

  "A LISAMessage" should "find objects with keys as" in {
    val res = mess.findObjectsWithKeysAs[SUBTest](List("key3", "key4"))
    res shouldEqual List(
      ("sub1", (SUBTest(1, false))),
      ("sub1", (SUBTest(2, true))))
  }
  "A LISAMessage" should "find no objects when no match on keys" in {
    val res = mess.findObjectsWithKeysAs[SUBTest](List("key3", "key4", "key100"))
    res shouldEqual List()
  }
  "A LISAMessage" should "find objects with fields" in {
    val res = mess.findObjectsWithField(List(("key3", JInt(1))))
    res shouldEqual List(
      ("sub1",JObject(
        List(("key3",JInt(1)), ("key4",JBool(false))))))
  }
  "A LISAMessage" should "find objects with fields as" in {
    val res = mess.findObjectsWithFieldAs[SUBTest](List(("key3", JInt(1))))
    res shouldEqual List(
      ("sub1", (SUBTest(1, false))))
  }

  "A LISAMessage" should "take new dynamic attributes" in {
    val sub = SUBTest(1, false)
    val mess = LISAMessage(JObject()) + ("ja"-> sub)
    println(s"A MESSAGE: $mess")
  }


  
  
}
