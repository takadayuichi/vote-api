package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import scala.util.parsing.json._
import scala.collection.mutable
import scala.collection.immutable
import com.mongodb.casbah.Imports._

object GetList extends Controller {

  // リクエストパラメータのnumを取得
  val form = Form("num" -> text)

  // MongoDB接続設定
  val conn = MongoConnection("mongo-takadayuichi-db-0.azva.dotcloud.net", 51406)
  val db = conn("vote")
  db.authenticate("vote", "kein1980")
  val questionCollection = db("test")
  val voteCollection = db("vote_test")

  // リクエスト処理
  def getList = Action { implicit request =>
    val param = form.bindFromRequest.get

    // ログ
    println("リクエスト" + param)

    val num = param.toInt
    var result = ""
    var resultList = List.empty[String]

    // 取得
    questionCollection.find().limit(num).foreach { rowObj =>
      val _id: Option[String] = rowObj.getAs[String]("_id")
      val title: Option[String] = rowObj.getAs[String]("title")
      val text: Option[String] = rowObj.getAs[String]("text")
      val username: Option[String] = rowObj.getAs[String]("username")

      val m = JSONObject(immutable.Map(
        "_id" -> _id,
        "title" -> title,
        "text" -> text,
        "username" -> username))

      resultList = m.toString() :: resultList
    }
    result = JSONArray(resultList).toString
    Ok(result)
  }
}