package replpp

/***
 * 编译解释结果集
 */
class CompileInterpretResult {

  private var result = ""

  def getResult(): String = this.result
  def setResult(str: String): Unit = this.result = str

}
