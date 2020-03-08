package hello

import zio.App
import zio.console.{ putStrLn }

object Main extends App {  
  def run(args: List[String]) =
    myAppLogic.map(_ => 1)

  val myAppLogic =
    for {
      _ <- putStrLn("Hello World")
    } yield ()
}
