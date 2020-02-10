package presentation

import cats.data.`package`.State

object Deterministic {
  final case class Seed(long: Long) {
    def next = Seed(long * 6364136223846793005L + 1442695040888963407L)
  }  
  
  val nextLong: State[Seed, Long] = State(seed =>
  (seed.next, seed.long))

  def rollDice() : Int =  {
    ((Math.random() * 6) + 1).toInt 
  }

  def rollDice2(): State[Seed, Int]  = for {
      dice <- nextLong
  } yield dice.toInt %6 + 1
  
}