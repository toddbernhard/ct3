package com.ltfme.loadbalancer

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.ltfme.loadbalancer.util.{Config, Server}
import play.api.http.HeaderNames
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.mvc.Http.Status

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * User: ilya
 * Date: 10/21/15
 * Time: 9:52 AM
 */
@ImplementedBy(classOf[PlayReverseProxyWithWS])
sealed trait PlayReverseProxy[RES] {

  def proxyToServer(request: Request[RawBuffer]): RES

  protected def headers(h: Map[String, Seq[String]], s: Server) =
    (h.toSeq.flatMap {
      case (k, v) => v.map((k, _))
    } filterNot { case (k, _) => k == "Host" }) :+
        ("Host" -> s.host.replaceFirst( """^https?://""", ""))

  protected def queryString(qs: Map[String, Seq[String]]) =
    qs.toSeq.flatMap {
      case (k, v) => v.map((k, _))
    }
}

@Singleton
class PlayReverseProxyWithWS @Inject()(ws: WSClient, stickyStrategy: StickyStrategy) extends PlayReverseProxy[Future[Result]] {
  override def proxyToServer(request: Request[RawBuffer]): Future[Result] = {

    val cookie = request.cookies.get(Config.cookieName)
    val server = cookie.map(c => stickyStrategy.server(c.value).getOrElse(stickyStrategy.selectServer)).
        getOrElse(stickyStrategy.selectServer)

    println(s"COOKIE: ${cookie.map(_.value).getOrElse("undefined")} - ${request.method} ${request.uri} --> ${server.host}")

    doProxy(request, server)
  }

  private def doProxy(request: Request[RawBuffer], server: Server): Future[Result] = {
    val proxyRequest =
      ws.url(server.host + request.uri)
          .withFollowRedirects(true)
          .withMethod(request.method)
          .withHeaders(headers(request.headers.toMap, server): _*)
          .withQueryString(queryString(request.queryString): _*)
          .withBody(request.body.asBytes().get)

    proxyRequest.get().map {
      r =>
        /*
        Here we normalize headers as per http header specification (you can either have multiple headers
        same name or a header can specify the values separated by a comma.
        */
        val resHeaders = r.allHeaders.
            filterKeys(!Seq(HeaderNames.TRANSFER_ENCODING, HeaderNames.CONTENT_LENGTH).contains(_)).
            toSeq.flatMap {
          case (k, v) => v.map((k, _))
        }

        new Results.Status(r.status).chunked(Enumerator(r.bodyAsBytes)).
            withHeaders(resHeaders: _*).
            withCookies(Cookie(Config.cookieName, server.name))
    }
  }
}



