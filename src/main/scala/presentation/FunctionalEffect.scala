package presentation

import console.{ Console, PrintLine, ReadLine, Return }
import console.ConsoleModel._
import console.ConsoleSyntax._

object FunctionalEffect {

  val example1: Console[String] =
    PrintLine("Hello, what is your name?", ReadLine(name => PrintLine(s"Good to meet you, ${name}", Return(() => (name)))))

  def basic() = {
    val example : Console[String] =
      PrintLine(
        "Sag hallo zur Konsole",
        ReadLine(line => PrintLine(s"Es wurde ${line} eingegeben", Return(() => line)))
      )
    val retval = interpret(example)
    println(s"Der Rueckgageberwert war ${retval}")
  }

  def advanced() = {
    val example2: Console[String] = for {
      _      <- printLine("Sag Hallo zur Konsole")
      line   <- readLine
      length <- succeed({ line.length().toString() })
      _      <- printLine(s"Es wurde ${line} mit der Laenge ${length} eingegeben")
    } yield line
    val retval2 = interpret(example2)
    println(s"Der Rueckgageberwert war ${retval2}")
  }

  def main(args: Array[String]): Unit = advanced()
}
