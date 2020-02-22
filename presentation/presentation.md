---
marp: true
theme: gaia
_class: lead
paginate: true
backgroundColor: #fff
backgroundImage: url('https://marp.app/assets/hero-background.jpg')
---


# **Functional effects**

Was machen functionale Sprachen eigentlich mit Seiteneffekten und anderem Zeugs

---
# Funktionen

Funktionen haben die folgenden Eigenschaften

* Total - Jeder Eingabe gibt ein Ausgabe
* Deterministisch - Die gleiche Eingabe gibt die gleiche Ausgabe
* Rein - Keine Seiteneffekte
  
---
# Total

Ein Beispiel fuer eine nicht totale Funktion

``` scala
def total(num: Int) : Int = {
     if (num <= 0)
      throw new IllegalArgumentException()
    else
      modified(num)
  }
```

Fuer einige Werte wift die Funktion eine Exception und hat keinen Wert

---

# Total - Either zur Rettung

``` scala
  def total2(num: Int) : Either[Exception,Int] = {
     if (num <= 0)
      Left(new IllegalArgumentException())
    else
      Right(modified(num))
  }
```

Mit Either kann auch im Fehlerfall ein Ergebnis ausgegeben werden.

---

# Deterministisch

Eine Beispiel fuer eine nicht deterministische Funktion

``` scala
public void rollDice() {
    int roll = (int) (Math.random() * 6) + 1;
    return roll
}
```
Bei jeden Aufruf kommt potentiell ein andere Wert raus

---

# Deterministisch - State als Retter (manchmal)

``` scala
def rollDice2(): State[Seed, Int]  = for {
      dice <- nextLong
  } yield dice.toInt %6 + 1
```

Das State Pattern kann vieles, aber nicht alles loesen

---
# Rein
Ein Beispiel fuer eine nicht pure Funktion

``` java
public void unpure() {
    System.out.println("Ich bin eine Seiteneffekt");
}
```

Die Funktion hat einen Seiteneffekt: Ausgabe auf Konsole.
Hier wird es schwierig.

---

# Functional Effects

* Kein Programm mit Sinn ist ohne Seiteneffekte
* Wenn wir das schon machen, dann am Rand unserers Programmes

---

# Modell eines Konsolen Programms

``` scala
sealed trait Console[+A]
final case class Return[A](value: () => A)                    extends Console[A]
final case class PrintLine[A](line: String, rest: Console[A]) extends Console[A]
final case class ReadLine[A](rest: String => Console[A])      extends Console[A]
```

``` scala
 def basic() = {
    val example =
      PrintLine(
        "Sag hallo zur Konsole",
        ReadLine(line => PrintLine(s"Es wurde ${line} eingegeben", Return(() => line)))
      )
    val retval = interpret(example)
    println(s"Der Rueckgageberwert war ${retval}")
  }  
```

---

# Machen wir es huebscher

Wir definieren Helferlein

``` scala
def succeed[A](a: => A): Console[A] = Return(() => a)
def printLine(line: String): Console[Unit] = PrintLine(line, succeed(()))
val readLine: Console[String] = ReadLine(line => succeed(line))
```

---

# Und noch zu einer Monade

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

---

# FP Konsole 2.0

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

---

# Functional Effect

* Ist letztlich Code als Wert
* Immutable und typensicher
* Wird in einer Umgebung interpretiert wird
* Ist in Haskell fest eingebaut
    
---

# Function Effect Frameworks

Es gibt diverse Frameworks

* Monix
* Cats effects
* ZIO

----

# Zio
![](zio.png)
Type-safe, composable asynchronous and concurrent programming

---

# Wichtigster Datentyp

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
---

# Fehler und Ergebnistyp

## Fehler

Wenn der Fehlertype Nothing ist. Hat der Effekt keinen Fehlerfall.

## Ergebnistyp
* Unit -> Kein sinnvolles Ergebnis. Nur fuer den Seiteneffekt
* Nothing -> Der Effekt laeuft ewig

---

# Environment

* Was braucht der Effekt um zu laufen?
* Guice ohne Reflektion.
* Mit voller Type Inference
* Der Compiler checked ob alles da ist

----
  
# Konstruieren von Effekten

``` scala
val s1: ZIO[Any, Nothing, String]            = ZIO.succeed("Hat geklappt")
val e1: ZIO[Any, IllegalStateException, Any] = ZIO.fail(new IllegalStateException())
val se1: ZIO[Any, Throwable, String]         = ZIO.effect(StdIn.readLine())
val sleeping: ZIO[Blocking, Throwable, Unit] = effectBlocking(Thread.sleep(Long.MaxValue))
```

---

# Chaining

_ZIO[R,E,A]_ ist eine Monade. Deswegen haben wir _flatmap_ und _map_

## Ohne for comprehension
``` scala
  val ex1 : ZIO[Console, Exception, Int] = {
    ZIO.environment[Console]
    .flatMap( c => c.console.getStrLn)
    .map( s=> s.length())
  }
```
---

# Chaining

In Scala kann man das schoener machen

## Mit for comprehension
```scala
  val ex2 : ZIO[Console, Exception, Int] = for {
    c <- ZIO.environment[Console]
    s <- c.console.getStrLn
  } yield s.length()
```
---

# Running effects

Effekte laufen in einer Runtime
``` scala
  def prog :  ZIO[Console, IOException, String] = for {
    input <- getStrLn
    _ <- putStr(s"Input String was $input")
  } yield input

  val progWithEnv : ZIO[Any, IOException, String] = prog.provide(Console.Live)
  val runtime = new DefaultRuntime {}
  runtime.unsafeRun(progWithEnv)
```

---

# But why

Angenommen wir haben zwei Effekte

``` scala
  def getConfigFromServer(): ZIO[Any, Exception, Config] = ???
  def getDefaultConfig(): ZIO[Any, Exception, Config]    = ???
```

Die wissen nichts von Thread oder sonst irgenwas. Aber da sie nur interpretiert werden.

Vielleicht kann ja ZIO helfen

---

# Machs nochmal Sam

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
---

# Oder parallel

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