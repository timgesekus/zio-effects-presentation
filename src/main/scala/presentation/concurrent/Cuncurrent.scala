package presentation.concurrent

import zio.ZIO
import zio.Schedule
import zio.clock.Clock
import zio.duration._

object Concurrent {
  case class Config()
  def getConfigFromServer(): ZIO[Any, Exception, Config] = ???
  def getDefaultConfig(): ZIO[Any, Exception, Config]    = ???

  def getConfig(): ZIO[Clock, Exception, Config] =
    getConfigFromServer()
      .retry(Schedule.recurs(4))
      .orElse(getDefaultConfig())

  def getConfig2(): ZIO[Clock, Exception, Config] =
    (getConfigFromServer().timeoutFail(new Exception("Timeout"))(1000.millis))
      .retry(Schedule.recurs(4))
      .orElse(getDefaultConfig())
  
  def getConfig3(): ZIO[Clock, Exception, Config] = for {
      config <- getConfigFromServer().race(getDefaultConfig())
  } yield config

  def getConfig4(): ZIO[Clock, Exception, Config] = for {
      fiber1 <- getConfigFromServer().fork
      fiber2 <- getDefaultConfig().fork
      fiber = fiber1.orElse(fiber2)
      config <- fiber.join
  } yield config
}
