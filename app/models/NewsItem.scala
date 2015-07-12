package models

import java.text.SimpleDateFormat
import anorm._
import anorm.SqlParser._
import java.util.Locale
import anorm.Column.columnToString

/**
 * Created by Richard Poole on 11/07/15.
 */
case class NewsItem(title: String, description: String, link: String, pubDate: String, category: String, enclosure: String) {
  def getPubDate: String = {
    val dateIn = new SimpleDateFormat("yyyyMMdd hh:mm:ss",Locale.ENGLISH)
    val dateOut = new SimpleDateFormat("EEE d MMM yyyy  @ hh:mm:ss",Locale.ENGLISH)
    dateOut.format(dateIn.parse(pubDate))
  }
}

object NewsItem extends ((
  String,
  String,
  String,
  String,
  String,
  String) => NewsItem) {

  /**
   * Map the result into a List[NewsItem]
   */
  val rowParser =
      str("title") ~
      str("description") ~
      str("link") ~
      str("pubDate") ~
      str("category") ~
      str("enclosure") map {
      case title ~
        description ~
        link ~
        pubDate ~
        category ~
        enclosure => NewsItem(title, description, link, pubDate, category, enclosure)
    }

  /**
   * Attempt to search SQLite database for whole words / phrases
   * Messy, would normally use regex word bounds but can't get library for regex in Play!
   * @param searchTerm String
   * @return
   */
  def findByString(searchTerm: String) = {
    DB.withConnection { implicit connection =>
      SQL( """SELECT * FROM newsitem WHERE
              description LIKE '% ' || {searchterm} || ' %' OR
              description LIKE '% ' || {searchterm} OR
              description LIKE {searchterm} || ' %' OR
              description = {searchterm} OR
              title LIKE '% ' || {searchterm} || ' %' OR
              title LIKE '% ' || {searchterm} OR
              title LIKE {searchterm} || ' %' OR
              title = {searchterm}
              ORDER BY pubDate DESC
           """ )
        .on('searchterm -> searchTerm,
            'searchterm -> searchTerm,
            'searchterm -> searchTerm,
            'searchterm -> searchTerm,
            'searchterm -> searchTerm,
            'searchterm -> searchTerm,
            'searchterm -> searchTerm,
            'searchterm -> searchTerm

        )
        .as(rowParser *)
    }
  }

  /**
   * Fetch all NewsItem records from the database
   * @return
   */
  def getAll() = {
    DB.withConnection { implicit connection =>
      SQL( "SELECT * FROM newsitem ORDER BY pubDate DESC" ).as(rowParser *)
    }
  }

  /**
   * Takes the NewsItem from RssParser and saves each record.
   * TODO: Chaining this onto the new NewsItem record created in RssParser.
   * @param newsitem Model[NewsItem]
   * @return Boolean TRUE: Record saved successfully FALSE: There was a problem saving.
   */
  def save(newsitem: NewsItem): Boolean = {
    if( !alreadyExists(newsitem.title, newsitem.pubDate) ){
      DB.withConnection { implicit connection =>
        SQL("""
            INSERT INTO newsitem
            VALUES({title},{description},{link},{pubDate},{category},{enclosure})
          """)
        .on('title -> newsitem.title,
            'description -> newsitem.description,
            'link -> newsitem.link,
            'pubDate -> newsitem.pubDate,
            'category -> newsitem.category,
            'enclosure -> newsitem.enclosure)
        .executeInsert() == 1l
      }
    }
    else {
      false
    }
  }

  def delete(newsitem: NewsItem): Boolean = {
    if( alreadyExists(newsitem.title, newsitem.pubDate) ){
      DB.withConnection { implicit connection =>
        SQL("""
            DELETE FROM newsitem WHERE
            title = {title} AND description = {description} AND pubDate = {pubDate}
            """)
        .on('title -> newsitem.title,
            'description -> newsitem.description,
            'pubDate -> newsitem.pubDate)
        .executeUpdate() == 1l
      }
    }
    else {
      false
    }
  }

  /**
   * Check if this record already exists in the database based on pubDate and title
   * @param title
   * @param pubDate
   * @return
   */
  def alreadyExists(title: String, pubDate: String): Boolean = {
    DB.withConnection { implicit connection =>
      val result: Long =
        SQL("""SELECT count(*) AS count FROM newsitem WHERE
             title = {newTitle} AND
             pubDate = {newPubDate}
        """)
        .on('newTitle -> title,
            'newPubDate -> pubDate)
        .as(scalar[Long].single)
      result > 0
    }
  }

}
