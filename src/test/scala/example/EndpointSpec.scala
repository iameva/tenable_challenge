package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import endpoint.{Endpoint, EndpointParams, JsonSupport}
import models.EndpointResponse
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class EndpointSpec extends FlatSpec with Matchers with JsonSupport {
  implicit val system = ActorSystem("Spec")
  implicit val materializer = ActorMaterializer()

  implicit def executionContext = system.dispatcher

  def queryLocalhost(uri: String): Future[EndpointResponse] = {
    Http()
      .singleRequest(HttpRequest(uri = uri))
      .flatMap(r => Unmarshal(r).to[EndpointResponse])
  }

  val baseUri = "http://localhost:33333/api/v1/components"

  def using[T <: AutoCloseable, R](generator: => T)(func: T => R): R = {
    val closeable = generator
    try {
      func(closeable)
    }
    finally {
      closeable.close()
    }
  }

  // note these tests rely on the service names of DigitalOcean

  "The Endpoint" should "accept requests with no name" in {
    using(new Endpoint(EndpointParams("localhost", 33333))) { endpoint =>
      Await.result(queryLocalhost(baseUri), 10.seconds) shouldEqual EndpointResponse(26)
    }
  }

  "The Endpoint" should "accept requests with one name and filter accordingly" in {
    using(new Endpoint(EndpointParams("localhost", 33333))) { endpoint =>
      Await.result(queryLocalhost(s"$baseUri?name=API"), 10.seconds) shouldEqual EndpointResponse(1)
    }
  }

  "The Endpoint" should "accept requests with multiple names" in {
    using(new Endpoint(EndpointParams("localhost", 33333))) { endpoint =>
      Await.result(queryLocalhost(s"$baseUri?name=API,AMS3"), 10.seconds) shouldEqual EndpointResponse(2)
    }
  }
}
