import com.ltfme.loadbalancer.util.Config
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.ws.WS
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ProxySpec extends Specification {

  implicit val timeout = 5 seconds

  "Application" should {

    "should reverse proxy google" in new WithApplication {
      route(FakeRequest(GET, "/").withCookies(Cookie(Config.cookieName, "second"))) match {
        case Some(r) =>
          headers(r).getOrElse("Server", "") must beEqualTo("gws")
          contentAsString(r) must contain("Google Search")
        case None => failure
      }

      var res = Await.result(
        WS.url("https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png").get(), timeout)

      route(FakeRequest(GET, "/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png").
          withCookies(Cookie(Config.cookieName, "second"))) match {
        case Some(r) =>
          headers(r).getOrElse("Server", "") must beEqualTo("sffe")
          contentAsBytes(r) must beEqualTo(res.bodyAsBytes)
        case None => failure
      }
    }


    "should reverse proxy yahoo" in new WithApplication {
      route(FakeRequest(GET, "/").withCookies(Cookie(Config.cookieName, "first"))) match {
        case Some(r) =>
          headers(r).getOrElse("Server", "") must beEqualTo("ATS")
          contentAsString(r) must contain("mail.yahoo.com")
        case None => failure
      }

      var res = Await.result(
        WS.url("https://www.yahoo.com").get(), timeout)

      route(FakeRequest(GET, "/").
          withCookies(Cookie(Config.cookieName, "first"))) match {
        case Some(r) =>
          headers(r).getOrElse("Server", "") must beEqualTo("ATS")
          headers(r).getOrElse("P3P", "bad") must beEqualTo(res.header("P3P").getOrElse("data"))
        case None => failure
      }
    }

  }

}
