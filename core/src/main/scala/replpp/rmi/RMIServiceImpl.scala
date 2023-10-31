package replpp.rmi

import dotty.tools.repl.State
import replpp.{Config, allPredefCode}

import java.rmi.server.UnicastRemoteObject
import scala.util.control.NoStackTrace

class RMIServiceImpl(config: Config) extends UnicastRemoteObject
  with replpp.rmi.RMIService {

  override def callBack1(line: String): String = {
    this.run(this.config, line)
    "hello, " + line
  }


  def run(config: Config, line: String): Unit = {

    val predefCode = allPredefCode(config) //预定义代码
    val compilerArgs = replpp.compilerArgs(config)
    import config.colors
    val rmiDriver = new RMIDriver(
      compilerArgs,
      onExitCode = config.onExitCode,
      greeting = config.greeting,
      prompt = config.prompt.getOrElse("scala"),
      maxHeight = config.maxHeight
    )

    val initialState: State = rmiDriver.initialState

    //处理预代码
    val state: State = rmiDriver.runQuietly(predefCode)(using initialState)
    if (predefCode.nonEmpty && state.objectIndex != 1) {
      throw new AssertionError(s"compilation error for predef code - error should have been reported above ^") with NoStackTrace
    }

    //    rmiDriver.run("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,run\")")(using state)
//    rmiDriver.runQuietly("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,runQuietly\") ")(using state)

    System.err.println("hello, RMI.start")
    rmiDriver.runQuietly(line)(using state)
    System.err.println("hello, RMI.end")
  }

}
