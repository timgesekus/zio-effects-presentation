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
