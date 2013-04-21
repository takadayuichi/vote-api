package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import scala.util.parsing.json._
import scala.collection.immutable
import com.mongodb.casbah.Imports._

object Vote extends Controller {

  // リクエストパラメータのqid,choiceを取得
  val form = Form( tuple("qid" -> text, "choice" -> number) )

  // MongoDB接続設定
  val conn = MongoConnection("mongo-takadayuichi-db-0.azva.dotcloud.net", 51406)
  val db = conn("vote")
  db.authenticate("vote", "kein1980")
  val questionCollection = db("test")
  val voteCollection = db("vote_test")

  // リクエスト処理
  def vote = Action { implicit request =>
    val param = form.bindFromRequest.get

    // ログ
    println("リクエスト" + param.toString)

    val qid = param._1
    val choice = param._2

    // ビルダーで順次項目をobjectに追加
    val builder = MongoDBObject.newBuilder
    builder += "qid" -> qid
    builder += "choice" -> choice
    val vote = builder.result

    // mongodbへインサート
    voteCollection += vote

    // 取得
    val question = questionCollection.findOne(MongoDBObject("_id" -> new ObjectId(qid))).get
    val vote1 = voteCollection.count(MongoDBObject("qid" -> qid, "choice" -> 1))
    val vote2 = voteCollection.count(MongoDBObject("qid" -> qid, "choice" -> 2))
    val vote3 = voteCollection.count(MongoDBObject("qid" -> qid, "choice" -> 3))
    val vote4 = voteCollection.count(MongoDBObject("qid" -> qid, "choice" -> 4))
    val vote5 = voteCollection.count(MongoDBObject("qid" -> qid, "choice" -> 5))

    val voteAll = vote1 + vote2 + vote3 + vote4 + vote5
    val voteVal2 = vote2 * 100 / voteAll
    val voteVal3 = vote3 * 100 / voteAll
    val voteVal4 = vote4 * 100 / voteAll
    val voteVal5 = vote5 * 100 / voteAll
    val voteVal1 = 100 - voteVal2 - voteVal3 - voteVal4 - voteVal5

    val choiceString1 = question.get("choice1")
    val choiceString2 = question.get("choice2")
    val choiceString3 = question.getOrElse("choice3", "")
    val choiceString4 = question.getOrElse("choice4", "")
    val choiceString5 = question.getOrElse("choice5", "")

    val choice1 = JSONObject(immutable.Map(
      "id" -> 1,
      "choice" -> choiceString1,
      "vote" -> voteVal1))

    val choice2 = JSONObject(immutable.Map(
      "id" -> 2,
      "choice" -> choiceString2,
      "vote" -> voteVal2))

    val choice3 = JSONObject(immutable.Map(
      "id" -> 3,
      "choice" -> choiceString3,
      "vote" -> voteVal3))

    val choice4 = JSONObject(immutable.Map(
      "id" -> 4,
      "choice" -> choiceString4,
      "vote" -> voteVal4))

    val choice5 = JSONObject(immutable.Map(
      "id" -> 5,
      "choice" -> choiceString5,
      "vote" -> voteVal5))

    var choicesList = List(choice1, choice2)
    if (choiceString3 != "") { choicesList = List(choice1, choice2, choice3) }
    if (choiceString4 != "") { choicesList = List(choice1, choice2, choice3, choice4) }
    if (choiceString5 != "") { choicesList = List(choice1, choice2, choice3, choice4, choice5) }

    val choices = JSONArray(choicesList)

    val m = JSONObject(immutable.Map(
      "_id" -> question("_id").toString,
      "title" -> question("title"),
      "text" -> question("text"),
      "choices" -> choices,
      "username" -> question("username"),
      "password" -> question.getOrElse("password", "")))

    Ok(m.toString())
  }
}