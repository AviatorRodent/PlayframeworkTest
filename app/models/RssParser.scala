package models

import java.text.SimpleDateFormat
import java.util.Locale
import scala.concurrent.Future
import scala.xml.{Elem, XML}
/**
 * Created by user on 11/07/15.
 */
class RssParser {}
object RssParser {

  /**
   * Run on separate thread asynchronously
   * TODO: Work out how to hook this into startup so not to run with each request
   * @return
   */
  def logXml = {
    Future {
      while( true ) {

        // I miss PHP's strtotime()
        val dateIn = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
        val dateOut = new SimpleDateFormat("yyyyMMdd hh:mm:ss",Locale.ENGLISH)

        // Parse the XML and populate NewsItem, them append to Seq[NewsItem]
        val rss: Elem = XML.load("http://www1.skysports.com/feeds/11095/news.xml")
        val newsitems: Seq[NewsItem] = {
          for (item <- rss \\ "item" ) yield {
            NewsItem(
              (item \\ "title").text,
              (item \\ "description").text,
              (item \\ "link").text,
              dateOut.format(dateIn.parse((item \\ "pubDate").text)),
              (item \\ "category").text,
              (item \\ "enclosure").\@("url")
            )
          }
        }
        // Iterate over each NewsItem in newsitems and save
        newsitems.foreach{ NewsItem.save(_) }

        // Sleep for 5 minutes
        Thread.sleep(300000)
      }
    }
  }
}
