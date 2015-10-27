package com.ltfme.loadbalancer

import javax.inject.{Inject, Singleton}

import com.ltfme.loadbalancer.util.{Config, Server}
import play.api.http.HeaderNames
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * User: ilya
 * Date: 10/21/15
 * Time: 9:52 AM
 */
sealed trait ReverseProxy[REQ, RES] {

  def proxyToServer(request: REQ): RES

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
class PlayReverseProxyWithWS @Inject()(ws: WSClient, stickyStrategy: StickyStrategy) extends ReverseProxy[Request[RawBuffer], Future[Result]] {
  override def proxyToServer(request: Request[RawBuffer]): Future[Result] = {

    val serverBoundAndReady = request.cookies.get(Config.cookieName) match {
      case Some(cookie) => stickyStrategy.server(cookie.value)
      case None => None
    }

    serverBoundAndReady match {
      case Some(s) => doProxy(request, s)
      case None =>
        println(s"REDIRECTING TO: ${url(request)}")
        Future(Results.Found(url(request)).
            withCookies(Cookie(Config.cookieName, stickyStrategy.selectServer.name)))
    }
  }

  private def url(request: Request[RawBuffer]) = {
    (if (request.secure) "https" else "http") + "://" +
        request.host + request.path +
        (if (request.rawQueryString.isEmpty) "" else s"?${request.rawQueryString}")
  }

  private def doProxy(request: Request[RawBuffer], server: Server): Future[Result] = {
    val proxyRequest =
      ws.url(server.host + request.path)
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

        println(s"HEADERS: $resHeaders")
        new Results.Status(r.status).chunked(Enumerator(r.bodyAsBytes)).withHeaders(resHeaders: _*)
    }
  }
}



