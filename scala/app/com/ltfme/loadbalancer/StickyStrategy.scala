package com.ltfme.loadbalancer

import com.google.inject.{Singleton, ImplementedBy}
import com.ltfme.loadbalancer.util.{Server, Config}

import scala.util.Random

@ImplementedBy(classOf[RandomStrategy])
sealed abstract class StickyStrategy {

  protected val serversByNameIndex = Map(Config.servers.map(s => (s.name, s)):_*)

  final def server(name:String) = serversByNameIndex.get(name)

  // To be implemented as a part of various server selection strategies
  def selectServer: Server
}

@Singleton
class RandomStrategy extends StickyStrategy {
  override def selectServer: Server = {
    Config.servers(Random.nextInt(Config.servers.size))
  }
}
