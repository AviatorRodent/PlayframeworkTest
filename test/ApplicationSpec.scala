import models.NewsItem
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.Logger

import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Sky Sports - Football")
    }

    "render the page with a search term" in new WithApplication{
      val dummyNewsItem: NewsItem = { NewsItem("JUnit Test","JUnit Test","JUnit Test","20150712 11:20:00","JUnit Test","JUnit Test") }
      val dummyNewsItem2: NewsItem = { NewsItem("JUnit EXCLUDE","JUnit EXCLUDE","JUnit EXCLUDE","20150712 11:20:00","JUnit EXCLUDE","JUnit EXCLUDE") }
      NewsItem.save(dummyNewsItem)
      NewsItem.save(dummyNewsItem2)
      val home = route(FakeRequest(GET, "/?searchTerm=JUnit%20Test")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("JUnit Test")
      contentAsString(home) must not contain ("JUnit EXCLUDE")

      NewsItem.delete(dummyNewsItem)
      NewsItem.delete(dummyNewsItem2)
    }
  }
}
