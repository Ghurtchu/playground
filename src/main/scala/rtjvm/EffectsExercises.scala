package rtjvm

object EffectsExercises {

  // how IO bridges the gap between pure vs impure programs

  /**
   * Execises
   * 1. An IO which returns current time of the system
   * 2. An IO which measures the duration of a computation
   */

  case class io[A](unsafeRun: () => A) {
    def map[B](f: A => B): io[B] = new io[B](() => f(unsafeRun()))
    def flatMap[B](f: A => io[B]): io[B] = new io[B](() => f(unsafeRun()).unsafeRun())
  }

  // 1
  val currentTime: io[Long] = io(() => System.currentTimeMillis())

  // 2
  // measure # of millis
  def measure[A](computation: io[A]): io[Long] =
    for {
      now <- currentTime
      _ <- computation
      now2 <- currentTime
    } yield now2 - now

  val readLine: io[String] = io(() => Console.in.readLine())

  def main(args: Array[String]): Unit = {
    val computation = io(() => println("boom"))

    println(measure(computation)
      .unsafeRun())

    println(measure(readLine).unsafeRun())

  }
}
