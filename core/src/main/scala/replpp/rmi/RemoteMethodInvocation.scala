package replpp.rmi

import dotty.tools.Settings
import dotty.tools.io.{ClassPath, Directory, PlainDirectory}
import dotty.tools.repl.State
import replpp.{Config, ReplDriver, allPredefCode, verboseEnabled}

import java.lang.System.lineSeparator
import scala.util.control.NoStackTrace

/***
 * RMI植入
 */
object RemoteMethodInvocation {

  def main(args: Array[String]): Unit = {
    val config = Config.parse(args)
    this.run(config)
  }

  def run(config: Config): Unit = {

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

//    val state: State = {
//      if (verboseEnabled(config)) {
//        println(s"compiler arguments: ${compilerArgs.mkString(",")}")
//        println(predefCode)
//        rmiDriver.run(predefCode)(using initialState)
//      } else {
//        rmiDriver.runQuietly(predefCode)(using initialState)
//      }
//    }

    //处理预代码
    val state: State = rmiDriver.runQuietly(predefCode)(using initialState)
    if (predefCode.nonEmpty && state.objectIndex != 1) {
      throw new AssertionError(s"compilation error for predef code - error should have been reported above ^") with NoStackTrace
    }

//    rmiDriver.run("val a = 1")(using state)

    //运行直到退出（获取ctrl+c或quit命令）
    rmiDriver.runUntilQuit(using state)()
  }

}
