---
marp: true
theme: gaia
_class: lead
paginate: true
backgroundColor: #fff
backgroundImage: url('https://marp.app/assets/hero-background.jpg')
headingDivider: 2
footer: "Tim Gesekus - Functional Effects"

---

# **Functional effects**

Was machen functionale Sprachen eigentlich mit Seiteneffekten und anderem Unanehmlichkeiten

## Was sind Effekte

Funktionale Effekte modelieren das Verhalten, dass eine Funktion haben kann.
* _Optional_ modelliert, dass ein Funktion kein Ergebnis haben kann.
* _Future_ modelliert "Verzögerung"
* _State_ modelliert, dass eine Funktion Zustand manipuliert
* _Either_ modelliert, dass eine Funktion einen Fehlerfall hat  
## Funktionen

Funktionen haben die folgenden Eigenschaften

* Total - Jeder Eingabe ergibt ein Ausgabe
* Deterministisch - Die gleiche Eingabe gibt immer die gleiche Ausgabe
* Rein - Keine Seiteneffekte
  
## Total

Ein Beispiel für eine nicht totale Funktion

``` scala
def total(num: Int) : Int = {
     if (num <= 0)
      throw new IllegalArgumentException()
    else
      modified(num)
  }
```

Für einige Werte wirft die Funktion eine Exception und hat keinen Wert

## Total - Either zur Rettung

``` scala
  def total2(num: Int) : Either[Exception,Int] = {
     if (num <= 0)
      Left(new IllegalArgumentException())
    else
      Right(modified(num))
  }
```

Mit Either kann auch im Fehlerfall ein Ergebnis ausgegeben werden.

## Deterministisch

Eine Beispiel für eine nicht deterministische Funktion

``` scala
public void rollDice() {
    int roll = (int) (Math.random() * 6) + 1;
    return roll
}
```

Bei jeden Aufruf kommt potentiell ein andere Wert raus

## Deterministisch - State als Retter (manchmal)

``` scala
def rollDice2(): State[Seed, Int]  = for {
      dice <- nextLong
  } yield dice.toInt %6 + 1
```

Das State Pattern kann vieles, aber nicht alles lösen

## Rein (Pure)

Ein Beispiel für eine nicht reine Funktion

``` java
public void unpure() {
    System.out.println("Ich bin eine Seiteneffekt");
}
```

Die Funktion hat einen Seiteneffekt: Ausgabe auf Konsole.
Hier wird es schwierig.

## Effekte

* Kein Programm mit Sinn ist ohne Seiten- und andere Effekte
* Wenn wir das schon machen, dann trennen wir diesen Teil ab.

Anhand der Konsolen Ausgabe spielen wir durch, wie man das in einer funktionalen Programmiersprache macht.

## Modell eines Konsolen Programms

``` scala
sealed trait Console[+A]
final case class Return[A](value: () => A)                    extends Console[A]
final case class PrintLine[A](line: String, rest: Console[A]) extends Console[A]
final case class ReadLine[A](rest: String => Console[A])      extends Console[A]
```

``` scala
  val example : Console[String] =
    PrintLine( "Sag hallo zur Konsole",
      ReadLine( line => PrintLine(s"Es wurde ${line} eingegeben", 
        Return(() => line)))
      )
    val retval = interpret(example)
    println(s"Der Rueckgageberwert war ${retval}")
```

## Der Interpreter

```scala
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
```

## Machen wir es hübscher

Wir definieren Helferlein

``` scala
def succeed[A](a: => A): Console[A] = Return(() => a)
def printLine(line: String): Console[Unit] = PrintLine(line, succeed(()))
val readLine: Console[String] = ReadLine(line => succeed(line))
```

## Und machen noch eine Monade daraus

``` scala

  implicit class ConsoleSyntax[+A](self: Console[A]) {
    def map[B](f: A => B): Console[B] =
      flatMap(a => succeed(f(a)))

    def flatMap[B](f: A => Console[B]): Console[B] =
      self match {
        case Return(value) => f(value())
        case PrintLine(line, next) =>
          PrintLine(line, next.flatMap(f))
        case ReadLine(next) =>
          ReadLine(line => next(line).flatMap(f))
      }
  }

```

## FP Konsole 2.0

``` scala
  def advanced() = {
    val example2: Console[String] = for {
      _      <- printLine("Sag Hallo zur Konsole")
      line   <- readLine
      length <- succeed({line.length().toString()})
      _      <- printLine(s"Es wurde ${line} mit der Laenge ${length} eingegeben")
    } yield line
    val retval2 = interpret(example2)
    println(s"Der Rueckgageberwert war ${retval2}")
  }
```

## Functional Effect

* Ist letztlich Code als Wert
* Immutable und typensicher
* Wird in einer Umgebung interpretiert
* Trennt reinen Code mit allen Vorteilen von FP vom Rest.

## Function Effect Frameworks

Es gibt diverse Frameworks

* Monix (Scala)
* Cats effects (Scala)
* ZIO (Scala)
* Functional Java (Java)
* Cyclops X (Java)

## Zio

![ZIO](zio.png)
Type-safe, composable asynchronous and concurrent programming

## Wichtigster Datentyp

``` scala
ZIO[R, E, A]
```

* R - Umgebung
* E - Fehler Typ
* A - Ergebnis Typ

Der Interpreter wandelt

``` scala
R => Either[E,A]
```

## Fehler und Ergebnistyp

### Fehler

Wenn der Fehlertype Nothing ist. Hat der Effekt keinen Fehlerfall.

### Ergebnistyp

* Unit -> Kein sinnvolles Ergebnis. Nur für den Seiteneffekt
* Nothing -> Der Effekt läuft ewig

## Environment

* Was braucht der Effekt um zu laufen?
* Guice ohne Reflektion.
* Mit voller Type Inference
* Der Compiler checked ob alles da ist

## Konstruieren von Effekten

``` scala
val s1: ZIO[Any, Nothing, String]            = ZIO.succeed("Hat geklappt")
val e1: ZIO[Any, IllegalStateException, Any] = ZIO.fail(new IllegalStateException())
val se1: ZIO[Any, Throwable, String]         = ZIO.effect(StdIn.readLine())
val sleeping: ZIO[Blocking, Throwable, Unit] = effectBlocking(Thread.sleep(Long.MaxValue))
```

## Chaining

_ZIO[R,E,A]_ ist eine Monade. Deswegen haben wir _flatmap_ und _map_

### Ohne for comprehension

``` scala
  val ex1 : ZIO[Console, Exception, Int] = {
    ZIO.environment[Console]
    .flatMap( c => c.console.getStrLn)
    .map( s=> s.length())
  }
```

## Chaining 2

In Scala kann man das schöner machen

### Mit for comprehension

```scala
  val ex2 : ZIO[Console, Exception, Int] = for {
    c <- ZIO.environment[Console]
    s <- c.console.getStrLn
  } yield s.length()
```

## Running effects

Effekte laufen in einer Runtime

``` scala
  def prog :  ZIO[Console, IOException, String] = ??? 

  val progWithEnv : ZIO[Any, IOException, String] = prog.provideLayer(Console.live)
  val runtime = Runtime.default
  runtime.unsafeRun(progWithEnv)
```

## Umgang mit Fehlern

``` scala
  val progWithError: ZIO[Console, IOException, String] = ???

  val progWithoutError ZIO[Any, Nothing, String] = prog.foldM(
    error => IO.succeed(s"Prog failed with $error"),
    success => IO.succeed(s"Prog succeeded with $success")
  )
  
  val progWithEnv: ZIO[Any, Nothing, String] = progWithoutError.provideLayer(Console.live)
  val out : String                           = Runtime.default.unsafeRun(progWithEnv)
  println(out)  
```

## But why

Angenommen wir haben zwei Effekte

``` scala
  def getConfigFromServer(): ZIO[Any, Exception, Config] = ???
  def getDefaultConfig(): ZIO[Any, Exception, Config]    = ???
```

Vielleicht hat ZIO ja ein paar Möglichkeiten

## Machs nochmal Sam

``` scala
def getConfig(): ZIO[Clock, Exception, Config] =
    getConfigFromServer()
      .retry(Schedule.recurs(4))
      .orElse(getDefaultConfig())
```

## Aber nicht so lang

``` scala
 def getConfig2(): ZIO[Clock, Exception, Config] =
    (getConfigFromServer().timeoutFail(new Exception("Timeout"))(1000.millis))
      .retry(Schedule.recurs(4))
      .orElse(getDefaultConfig())
```

## Oder parallel

```scala
  def getConfig4(): ZIO[Clock, Exception, Config] = for {
      fiber1 <- getConfigFromServer().fork
      fiber2 <- getDefaultConfig().fork
      fiber = fiber1.orElse(fiber2)
      config <- fiber.join
  } yield config
  ```

## Wer gewinnt

```scala
  def getConfig3(): ZIO[Clock, Exception, Config] = for {
      config <- getConfigFromServer().race(getDefaultConfig())
  } yield config
```
## ZIO liefert viele Datenstypen als Effekt:

* Promise
* Queue
* Stream
* Fiber

## Fazit

* Code als Wert zu modellieren ermöglicht
  * Den Kontext der Ausführung zu wählen
  * Reinen und nicht reinen Code zu trennen
* Funktionale Effekt Frameworks eröffnen viele Möglichkeiten
* Es gibt Implementierungen in Java. :)

## Beispiel

Kleines Beispiel ist im Ordner _src/main/scala/presentation/atm_
* Definition von Services
* Abhängigkeiten zwischen Services
* Effekt laufen lassen

## Referenzen

* [ZIO](https://zio.dev)
* [Cyclops X](https://github.com/aol/cyclops)

