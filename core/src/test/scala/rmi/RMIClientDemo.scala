package rmi

import replpp.rmi.RMIService

import java.rmi.Naming

object RMIClientDemo {
  def main(args: Array[String]): Unit = {
    try {

      val remoteAddr = "rmi://127.0.0.1:1010/hello"
      val rmiService = Naming.lookup(remoteAddr).asInstanceOf[RMIService]
      val res = rmiService.callBack1("hl")
      println(res)
    } catch {
      case e: Exception => println(e)
    }
  }
}
