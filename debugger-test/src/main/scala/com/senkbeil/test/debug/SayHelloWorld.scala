package debug

//import scala.io.StdIn
import Console.{in => StdIn}

object SayHelloWorld {

  def main(args: Array[String]) {
    val name = StdIn.readLine()
    println(s"Hello, $name")
  }

}