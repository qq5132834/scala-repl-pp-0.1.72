package replpp.rmi

import dotty.tools.repl.State
import replpp.{Config, allPredefCode}

import java.rmi.server.UnicastRemoteObject
import scala.util.control.NoStackTrace

import replpp.CompileInterpretResult

class RMIServiceImpl(config: Config) extends UnicastRemoteObject
  with replpp.rmi.RMIService {

  override def callBack1(line: String): String = {
    val res = this.run(this.config, line)
    res
  }


  def run(config: Config, line: String): String = {

    val predefCode = allPredefCode(config) //预定义代码
    val compilerArgs = replpp.compilerArgs(config)
    val phaseResult = CompileInterpretResult()

    import config.colors
    val rmiDriver = new RMIDriver(
      compilerArgs,
      scala.Console.out,
      onExitCode = config.onExitCode,
      greeting = config.greeting,
      prompt = config.prompt.getOrElse("scala"),
      maxHeight = config.maxHeight,
      None,
      phaseResult
    )


    this.m1(predefCode, line) //方案2
//    this.m2(rmiDriver, predefCode, line, phaseResult) //方案1，仿server方式

  }

  def m2(rmiDriver: RMIDriver, predefCode: String, line: String, phaseResult: CompileInterpretResult): String = {
    //方案2：
    val initialState: State = rmiDriver.initialState
    //处理预代码
    val state: State = rmiDriver.runQuietly(predefCode)(using initialState)
    if (predefCode.nonEmpty && state.objectIndex != 1) {
      throw new AssertionError(s"compilation error for predef code - error should have been reported above ^") with NoStackTrace
    }

    //    rmiDriver.run("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,run\")")(using state)
    //    rmiDriver.runQuietly("val a = 1 \n val b = 2 \n println(a+b) \n println(\"hello,runQuietly\") ")(using state)

    System.err.println("hello, RMI.start:" + line)
    //    rmiDriver.runQuietly(line)(using state)
    rmiDriver.run(line)(using state)
    System.err.println("hello, RMI.end")

    phaseResult.getResult()
  }

  def m1(predefCode: String, line: String):String = {
    //方案1：仿server模
    val rmiQueryResult = RmiRepl().query(predefCode + "\n" + line)
    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>1<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    System.out.println(rmiQueryResult.output)
    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>2<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    System.out.println(String.valueOf(rmiQueryResult))
    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>3<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    String.valueOf(rmiQueryResult)
  }

}
