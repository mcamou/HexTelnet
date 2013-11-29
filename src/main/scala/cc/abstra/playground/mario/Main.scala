package test

import java.net.{ Socket, SocketException }
import scala.concurrent._
import scala.annotation.tailrec
import java.io.{OutputStream, InputStream}

/**
 * Main program
 */
object Main {
  /**
   * Main entry point
   */
  def main(args:Array[String]) = {

    @tailrec
    def readFromNetwork(in: InputStream) {
      val b = in.read
      Console.out.print(f":$b%02X:")
      Console.out.flush
      if (b >= 0) readFromNetwork(in)
    }

    @tailrec
    def readFromKeyboard(out: OutputStream) {
      print(">>> ")
      val line = Console.in.readLine.toUpperCase

      if (line != "EOT" && line != "QUIT") {
        val bytes = hex2Bytes(line)

        Console.out.print("Sending ")
        bytes.foreach { b => Console.out.print(f"$b%02X ") }
        Console.out.println

        out.write(bytes)
        out.flush

        readFromKeyboard(out)
      }
    }

    import ExecutionContext.Implicits.global

    val port = args(1).toInt
    val host = args(0)
    val socket = new Socket(host, port)

    println (s"Connected to $host:$port")
    println ("Type 'EOT' or 'QUIT'' to exit")

    val in = socket.getInputStream
    val out = socket.getOutputStream

    future {
             try {
               readFromNetwork(in)
             } catch {
               case ex: SocketException =>
                 println("Socket closed by remote")
                 System exit 0
             }
           }


    try {
      readFromKeyboard(out)
    } catch {
      case ex: SocketException =>
        println("Socket closed by remote")
        System exit 0
    }
  }

  /**
   * Parse a hexadecimal String and return a new Array[Byte]
   * @param hex The hex string
   * @return a new Array[Byte] that contains the corresponding bytes
   */
  def hex2Bytes(hex: String): Array[Byte] = hex.grouped(2).map(Integer.parseInt(_, 16).toByte).toArray
}
