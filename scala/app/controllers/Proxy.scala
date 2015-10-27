package controllers

import javax.inject.Inject

import com.ltfme.loadbalancer.PlayReverseProxy
import play.api.mvc._

import scala.concurrent.Future

class Proxy @Inject()(proxy:PlayReverseProxy[Future[Result]]) extends Controller {
  def balance(path: String) = Action.async(parse.raw) {
    request =>
      proxy.proxyToServer(request)
  }
}