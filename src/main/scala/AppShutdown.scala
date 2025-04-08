object AppShutdown extends App {

  println("start")
  Thread sleep 1000

  sys.exit(1)

  println("end")
}
