package presentation

import zio.console.Console
import zio.ZIO

object Chaining {
  val ex1 : ZIO[Console, Exception, Int] = {
    ZIO.environment[Console]
    .flatMap( c => c.getStrLn)
    .map( s=> s.length())
  }

  val ex2 : ZIO[Console, Exception, Int] = for {
    c <- ZIO.environment[Console]
    s <- c.console.getStrLn
  } yield s.length()

}