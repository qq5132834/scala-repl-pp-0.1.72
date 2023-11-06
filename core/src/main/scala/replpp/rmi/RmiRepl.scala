package replpp.rmi

import dotty.tools.repl.State
import replpp.Colors.BlackWhite
import replpp.{CompileInterpretResult, ReplDriverBase, pwd}

import java.io.*
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}

case class RmiQueryResult(output: String, success: Boolean)

class RmiRepl(predefLines: IterableOnce[String] = Seq.empty) {

  /** repl and compiler output ends up in this replOutputStream */
  private val replOutputStream = new ByteArrayOutputStream()

  private val replDriver: ReplDriver = {
    val inheritedClasspath = System.getProperty("java.class.path")
    val compilerArgs = Array(
      "-classpath",
      inheritedClasspath,
      "-explain", // verbose scalac error messages. 详细标量错误消息。
      "-deprecation",
      "-color",
      "never"
    )
    val phaseResult = CompileInterpretResult()
    new ReplDriver(compilerArgs, new PrintStream(replOutputStream), classLoader = None, phaseResult)
  }

  private var state: State = {
    val state = replDriver.execute(predefLines)(using replDriver.initialState)
    val output = readAndResetReplOutputStream()
    if (output.nonEmpty)
      System.out.println(output)
    state
  }

  private val singleThreadedJobExecutor: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  private def queryAsync(inputLines: IterableOnce[String]): (UUID, Future[String]) = {
    val uuid = UUID.randomUUID()
    val future = Future {
      state = replDriver.execute(inputLines)(using state)
      readAndResetReplOutputStream()
    } (using singleThreadedJobExecutor)

    (uuid, future)
  }

  private def readAndResetReplOutputStream(): String = {
    val result = replOutputStream.toString(StandardCharsets.UTF_8)
    replOutputStream.reset()
    result
  }

  /** Submit query to the repl, await and return results. */
  def query(code: String): RmiQueryResult =
    query(code.linesIterator)

  /** Submit query to the repl, await and return results. */
  def query(inputLines: IterableOnce[String]): RmiQueryResult = {
    val (uuid, futureResult) = queryAsync(inputLines)
    val result = Await.result(futureResult, Duration.Inf)
    RmiQueryResult(result, success = true)
  }

  /** Shutdown the embedded shell and associated threads.
    */
  def shutdown(): Unit = {
    System.out.println("shutting down")
    singleThreadedJobExecutor.shutdown()
  }
}

class ReplDriver(args: Array[String], out: PrintStream, classLoader: Option[ClassLoader], phaseResult: CompileInterpretResult)
  extends ReplDriverBase(args, out, maxHeight = None, classLoader, phaseResult)(using BlackWhite) {
  def execute(inputLines: IterableOnce[String])(using state: State = initialState): State =
    interpretInput(inputLines, state, pwd)
}
