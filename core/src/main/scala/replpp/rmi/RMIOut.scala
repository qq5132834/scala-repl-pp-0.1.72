//package replpp.rmi
//
//import java.io.{OutputStream, PrintStream}
//
//class RMIOut(autoFlush: Boolean,
//             out: OutputStream)
//  extends PrintStream(autoFlush: Boolean,
//    out: OutputStream){
//
//  var resStr: String = ""
//
//  def getResStr(): String = {
//    this.resStr
//  }
//
//  def cleanResStr(): Unit = {
//    this.resStr = ""
//  }
//
//
//
//  override def print(s: String): Unit = super.print(s)
//
//  override def println(x: String): Unit = super.println(x)
//}
