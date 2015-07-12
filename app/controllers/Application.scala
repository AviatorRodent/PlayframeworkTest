package controllers

import models.NewsItem
import models.RssParser
import play.api._
import play.api.mvc._


class Application extends Controller {

  // Set implicits to be found by views
  implicit val brand: String = "Sky Sports"

  def index(searchTerm: String) = Action {
    RssParser.logXml
    val newsitems: List[NewsItem] = {
      if ( searchTerm != "" ){
        NewsItem.findByString(searchTerm)
      } else {
        NewsItem.getAll
      }
    }
    Ok(views.html.index("Sky Sports - Football",newsitems))
  }

}
