package presentation

object TotalExample {
  def modified(i: Int) : Int = i

  def total(num: Int) : Int = {
     if (num <= 0)
      throw new IllegalArgumentException()
    else
      modified(num)
  }
  
  def total2(num: Int) : Either[Exception,Int] = {
     if (num <= 0)
      Left(new IllegalArgumentException())
    else
      Right(modified(num))
  }
}