package presentation.runeffect
import zio.ZIO
import zio.console
import zio.console.Console
import java.io.IOException
import zio.Runtime

object RunningEffects {

  def prog: ZIO[Console, IOException, String] =
    for {
      input <- console.getStrLn
      _     <- console.putStr(s"Input String was $input")
    } yield input

  def main(args: Array[String]): Unit = {
    val progWithEnv: ZIO[Any, IOException, String] = prog.provideLayer(Console.live)
    val out = Runtime.default.unsafeRun(progWithEnv)
    println (out)
  }
}
