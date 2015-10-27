package controllers

import javax.inject.Inject

import com.ltfme.loadbalancer.PlayReverseProxyWithWS
import com.ltfme.loadbalancer.util.Config
import play.api._
import play.api.http.{HeaderNames, HttpProtocol}
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws.{WSClient, WSResponseHeaders, WS}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import scala.util.Random

class Proxy @Inject()(proxy:PlayReverseProxyWithWS) extends Controller {
  def balance(path: String) = Action.async(parse.raw) {
    request =>
      proxy.proxyToServer(request)
  }
}