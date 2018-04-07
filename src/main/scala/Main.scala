import endpoint.{Endpoint, EndpointParams}

import scala.util.Try

object Main {
  def main(args: Array[String]): Unit = {
    args match {
      case Array(host, port) if Try(port.toInt).isSuccess =>
        new Endpoint(EndpointParams(host, port.toInt))
      case _ =>
        println(s"Please start the app with two parameters, a valid host name to bind to and the port to listen on")
    }
  }
}
