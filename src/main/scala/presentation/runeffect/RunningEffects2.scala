package presentation.runeffect
import  zio.ZIO
import zio.console._
import java.io.IOException
import zio.Runtime


object RunningEffects2 {
 
  def prog :  ZIO[Console, IOException, String] = for {
    input <- getStrLn
    _ <- putStr(s"Input String was $input")
  } yield input

  val progWithEnv : ZIO[Any, IOException, String] = prog.provideLayer(Console.live)
  val runtime = Runtime.default
  runtime.unsafeRun(progWithEnv)
}