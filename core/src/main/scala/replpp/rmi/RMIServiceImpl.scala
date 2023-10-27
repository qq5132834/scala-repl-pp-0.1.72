package replpp.rmi

import java.rmi.server.UnicastRemoteObject

class RMIServiceImpl extends UnicastRemoteObject
  with replpp.rmi.RMIService {

  override def callBack1(line: String): String = "hello, " + line

}
