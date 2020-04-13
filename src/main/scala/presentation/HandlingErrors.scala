package presentation.runeffect
import zio.ZIO
import zio.IO
import zio.console
import zio.console.Console
import java.io.IOException
import zio.Runtime

object HandlingErrors {

  def prog: ZIO[Console, IOException, String] =
    for {
      input <- console.getStrLn
      _     <- console.putStr(s"Input String was $input")
    } yield input

  def main(args: Array[String]): Unit = {
    val progWithoutError = prog.foldM(
      error => IO.succeed(s"Prog failed with $error"),
      success => IO.succeed(s"Prog succeeded with $success")
    )
    val progWithEnv: ZIO[Any, Nothing, String] = progWithoutError.provideLayer(Console.live)
    val out                                    = Runtime.default.unsafeRun(progWithEnv)
    println(out)
  }
}
