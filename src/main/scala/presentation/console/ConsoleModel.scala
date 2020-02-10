package presentation.console

sealed trait Console[+A]
final case class Return[A](value: () => A)                    extends Console[A]
final case class PrintLine[A](line: String, next: Console[A]) extends Console[A]
final case class ReadLine[A](next: String => Console[A])      extends Console[A]

object ConsoleModel {
  
  def succeed[A](a: => A): Console[A] = Return(() => a)
  def printLine(line: String): Console[Unit] =
    PrintLine(line, succeed(()))
  val readLine: Console[String] =
    ReadLine(line => succeed(line))

  def interpret[A](console: Console[A]): A =
    console match {
      case PrintLine(line, next) => {
        println(line)
        interpret(next)
      }
      case ReadLine(next) => {
        interpret(next(scala.io.StdIn.readLine()))
      }
      case Return(value) => value()
    }

  
}
