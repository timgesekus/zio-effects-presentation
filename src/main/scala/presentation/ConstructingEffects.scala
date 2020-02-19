package presentation

import zio.ZIO
import scala.io.StdIn
import java.lang.IllegalStateException
import zio.blocking._

object ConstructingEffects {
  val s1: ZIO[Any, Nothing, String]            = ZIO.succeed("Hat geklappt")
  val e1: ZIO[Any, IllegalStateException, Any] = ZIO.fail(new IllegalStateException())
  val se1: ZIO[Any, Throwable, String]         = ZIO.effect(StdIn.readLine())
  val sleeping: ZIO[Blocking, Throwable, Unit] =
    effectBlocking(Thread.sleep(Long.MaxValue))
}
