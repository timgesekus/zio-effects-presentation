package presentation
import zio.ZIO

object Env {
  trait MyService {
    def getInt() : ZIO[Any,Nothing, Int] = ZIO.succeed(4) 
  }
  
  val env: ZIO[MyService, Nothing, Int] = for {
    m <-  ZIO.environment[MyService]
    i <- m.getInt
  } yield i
}