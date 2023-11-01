package replpp.rmi

import dotty.tools.Settings
import dotty.tools.io.{ClassPath, Directory, PlainDirectory}
import dotty.tools.repl.State
import replpp.{Config, ReplDriver, allPredefCode, verboseEnabled}

import java.lang.System.lineSeparator
import scala.util.control.NoStackTrace

import replpp.CompileInterpretResult

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
    val result = CompileInterpretResult()
    import config.colors
    val rmiDriver = new RMIDriver(
      compilerArgs,
      onExitCode = config.onExitCode,
      greeting = config.greeting,
      prompt = config.prompt.getOrElse("scala"),
      maxHeight = config.maxHeight,
      None,
      result
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

//    rmiDriver.run("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,run\") \n ")(using state)
//    rmiDriver.runQuietly("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,runQuietly\") ")(using state)


    try {
      val rmiService: replpp.rmi.RMIService = new RMIServiceImpl(config)
      java.rmi.registry.LocateRegistry.createRegistry(1010)
      // 将远程对象注册到 RMI 注册服务器上，并命名为 Hello
      java.rmi.Naming.bind("rmi://127.0.0.1:1010/hello", rmiService)
      System.err.println("RMI服务器启动成功！")
    } catch {
      case e: Exception =>
        System.err.println(e.getClass.getName)
        e.printStackTrace()
    }

  }

}
