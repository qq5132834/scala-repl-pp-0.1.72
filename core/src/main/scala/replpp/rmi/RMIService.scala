package replpp.rmi

import java.rmi.Remote

trait RMIService extends Remote {

  @throws(classOf[java.rmi.RemoteException])
  def callBack1(line: String) : String
}
