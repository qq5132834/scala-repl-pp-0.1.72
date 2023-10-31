package replpp.rmi

import dotty.tools.dotc.core.Contexts
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.repl.*
import org.jline.reader.*
import replpp.rmi.RMIServiceImpl
import replpp.{Colors, ReplDriverBase, pwd}

import java.io.PrintStream
import java.rmi.Naming
import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

class RMIDriver(args: Array[String],
                out: PrintStream = scala.Console.out,
                onExitCode: Option[String] = None,
                greeting: Option[String],
                prompt: String,
                maxHeight: Option[Int] = None,
                classLoader: Option[ClassLoader] = None)(using Colors)
  extends RMIDriverBase(args, out, maxHeight, classLoader) {

  /** Run REPL with `state` until `:quit` command found. 使用“state”运行REPL，直到找到“：quit”命令为止。
    * Main difference to the 'original': different greeting, trap Ctrl-c. 与“原始”的主要区别：不同的问候语、捕获Ctrl-c。
   */
  override def runUntilQuit(using initialState: State = initialState)(): State = {
    val terminal = new replpp.JLineTerminal {
      override protected def promptStr = prompt
    }
    greeting.foreach(out.println)

    @tailrec
    def loop(using state: State)(): State = {
      Try {
        System.err.println("循环等待输入")
        val inputLines = readLine(terminal, state)
        interpretInput(inputLines, state, pwd)
      } match {
        case Success(newState) =>
          loop(using newState)()
        case Failure(_: EndOfFileException) =>
          // Ctrl+D -> user wants to quit
          onExitCode.foreach(code => run(code)(using state))
          state
        case Failure(_: UserInterruptException) =>
          // Ctrl+C -> swallow, do nothing
          loop(using state)()
        case Failure(exception) =>
          throw exception
      }
    }

//    try {
//      val rmiService:replpp.rmi.RMIService = new RMIServiceImpl()
//      java.rmi.registry.LocateRegistry.createRegistry(1010)
//      // 将远程对象注册到 RMI 注册服务器上，并命名为 Hello
//      java.rmi.Naming.bind("rmi://127.0.0.1:1010/hello", rmiService)
//      System.err.println("RMI服务器启动成功！")
//    } catch {
//      case e: Exception =>
//        System.err.println(e.getClass.getName)
//        e.printStackTrace()
//    }

    try runBody {
      System.err.println("开始执行循环................")
      loop(using initialState)()
    }
    finally terminal.close()
  }

  /** Blockingly read a line, getting back a parse result.
    * The input may be multi-line.
    * If the input contains a using file directive (e.g. `//> using file abc.sc`), then we interpret everything up
    * until the directive, then interpret the directive (i.e. import that file) and continue with the remainder of
    * our input. That way, we import the file in-place, while preserving line numbers for user feedback.  */
  private def readLine(terminal: replpp.JLineTerminal, state: State): IterableOnce[String] = {
    given Context = state.context
    val completer: Completer = { (_, line, candidates) =>
      val comps = completions(line.cursor, line.line, state)
      candidates.addAll(comps.asJava)
    }
    terminal.readLine(completer).linesIterator
  }

}
