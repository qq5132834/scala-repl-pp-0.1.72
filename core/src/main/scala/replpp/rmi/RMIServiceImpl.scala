package replpp.rmi

import dotty.tools.repl.State
import replpp.{Config, allPredefCode}

import java.rmi.server.UnicastRemoteObject
import scala.util.control.NoStackTrace

class RMIServiceImpl(config: Config) extends UnicastRemoteObject
  with replpp.rmi.RMIService {

  override def callBack1(line: String): String = {
    var res = this.run(this.config, line)
//    "hello, " + line
    res
  }


  def run(config: Config, line: String): String = {

    val predefCode = allPredefCode(config) //预定义代码
    val compilerArgs = replpp.compilerArgs(config)
    val rmiOutputStream = new RmiOutputStream("D:/test.txt", "")
//    rmiOutputStream.setStr("")
    import config.colors
    val rmiDriver = new RMIDriver(
      compilerArgs,
//      scala.Console.out,
      rmiOutputStream,
      onExitCode = config.onExitCode,
      greeting = config.greeting,
      prompt = config.prompt.getOrElse("scala"),
      maxHeight = config.maxHeight,
      None
    )

    val initialState: State = rmiDriver.initialState

    //处理预代码
    val state: State = rmiDriver.runQuietly(predefCode)(using initialState)
    if (predefCode.nonEmpty && state.objectIndex != 1) {
      throw new AssertionError(s"compilation error for predef code - error should have been reported above ^") with NoStackTrace
    }

//        rmiDriver.run("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,run\")")(using state)
//    rmiDriver.runQuietly("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,runQuietly\") ")(using state)

    System.err.println("hello, RMI.start:" + line)
//    rmiDriver.runQuietly(line)(using state)
    rmiDriver.run(line)(using state)
    System.err.println("hello, RMI.end")

    rmiOutputStream.getStr
  }

}
