package com.ltfme.loadbalancer.util

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

/**
 * User: ilya
 * Date: 10/20/15
 * Time: 5:30 PM
 */
object Config {
  val config = ConfigFactory.load("proxy")

  lazy val servers = config.getConfigList("servers").map {
    c => Server(c.getString("name"), c.getString("host"))
  }

  lazy val cookieName = config.getString("proxy.cookie-name")

}


case class Server(name:String, host:String)
