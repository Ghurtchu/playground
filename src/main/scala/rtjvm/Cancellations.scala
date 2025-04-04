//package rtjvm
//
//import cats.effect.{FiberIO, IO, IOApp, Poll}
//import utils.IO_Ops
//
//import scala.concurrent.duration.DurationInt
//
//object Cancellations extends IOApp.Simple {
//
//  /**
//   * Cancelling IOs so far
//   * - fib.cancel
//   * - IO.race & other APIs
//   * Manual cancellation
//   * -
//   */
//
//  // anything after IO.canceled will be canceled
//  val chainOfIOs: IO[Int] = IO("waiting").Debug >> IO.canceled >> IO(42).Debug
//
//  // uncancelable
//  // example: online store, payment processor
//  // payment process must NOT be canceled
//  val specialPaymentSystem =
//    (
//      IO("Payment running, don't cancel me...").Debug >>
//      IO.sleep(1.second) >>
//      IO("Payment completed.").Debug
//    ).onCancel(IO("MEGA CANCEL OF DOOM").Debug.void)
//
//  val cancellationOfDoom: IO[Unit] = for {
//    fib <- specialPaymentSystem.start
//    _ <- IO.sleep(500.millis)
//    _ <- fib.cancel
//  } yield ()
//
//  val atomicPayment = IO.uncancelable { poll: Poll[IO] =>
//    specialPaymentSystem // masking
//  }
//
//  // same as atomicPayment
//  val atomicPayment_v2 = specialPaymentSystem.uncancelable
//
//  // cancellation will be ignored here
//  val noCancellationOfDoom = for {
//    fib <- atomicPayment.start
//    _ <- IO.sleep(500.millis) >> IO("attempting cancellation...").Debug >> fib.cancel
//    _ <- fib.join
//  } yield ()
//
//  // Poll can be used to cancel IO-s within the big IO
//
//  /**
//   * example: authentication service, has two parts:
//   * - input password, can be cancelled, because otherwise we might block indefinitely on user input
//   * - verify password, CANNOT be cancelled once it's started
//   */
//
//  val inputPassword: IO[String] = IO("Input password:").Debug >> IO("(typing password)").Debug >> IO.sleep(5.seconds) >> IO("RockTheJVM1!")
//  val verifyPassword: String => IO[Boolean] = (pw: String) => IO("verifying...").Debug >> IO.sleep(5.seconds) >> IO(pw == "RockTheJVM1!")
//
//  val authFlow: IO[Unit] = IO.uncancelable { poll =>
//    for {
//      // this IO becomes cancellable again
//      pw <- poll(inputPassword).onCancel(IO("Authentication timed out.. Try again later").Debug.void)
//      verified <- verifyPassword(pw)
//      _ <- if (verified) IO("Authentication successful.").Debug else IO("Authentication failed.").Debug
//    } yield ()
//  }
//
//  val authProgram = for {
//    authFib <- authFlow.start
//    _ <- IO.sleep(3.seconds) >> IO("Authentication timeout, attempting cancel...").Debug >> authFib.cancel
//    _ <- authFib.join
//  } yield ()
//
///**
// * Exercises
// *
// */
//  // 1
//  val cancelBeforeMol = IO.canceled >> IO(42).Debug
//  // IO.unancelable eliminates cancelations
//  val uncancelableMol = IO.uncancelable(_ => IO.canceled >> IO(42).Debug)
//
//  // uncancelable will eliminate ALL cancel points
//
//  // 2
//  val invincibleAuthProgram = for {
//    authFib <- IO.uncancelable(_ => authFlow).start
//    _ <- IO.sleep(3.seconds) >> IO("Auth timeout, attempting cancel...").Debug >> authFib.cancel
//    _ <- authFib.join
//  } yield ()
//
//  def threeStepProgram: IO[Unit] = {
//    val sequence = IO.uncancelable { poll =>
//      poll(
//        IO("cancelable").Debug >> IO.sleep(1.second) >>
//          IO("uncancelable").Debug >> IO.sleep(1.second) >>
//          IO("second cancelable").Debug >> IO.sleep(1.second)
//      )
//    }
//
//    for {
//     fib: FiberIO[Unit] <- sequence.start
//     _   <- IO.sleep(1500.millis) >> IO("CANCELING").Debug >> fib.cancel
//     _   <- fib.join
//    } yield ()
//  }
//
//  override val run: IO[Unit] = threeStepProgram.void
//}
