package endpoint

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{get, parameters, path, _}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import models.EndpointResponse
import models.digital_ocean.{CompactComponent, Component, ComponentsResponse}
import org.apache.logging.log4j.scala.Logging
import scalikejdbc.{ConnectionPool, DB, _}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

// Represents the Endpoint Proxy service
// Creating an instance of this class will start the service
class Endpoint(params: EndpointParams) extends Logging with JsonSupport with AutoCloseable {

  // this should come from configuration eventually
  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:mem:hello", "user", "pass")
  // and we shouldn't need to create tables in this way
  Try(DB autoCommit { implicit session =>
    sql"CREATE TABLE records (composite_id text, name text, status text)".execute.apply()
  })


  implicit val system = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit def executionContext = system.dispatcher

  // maybe from configuration?
  val validComponentStatuses: Set[String] = Set(
    "operational",
    "degraded_performance",
    "partial_outage",
    "major_outage"
  )

  def isValidComponent(component: Component): Boolean = {
    validComponentStatuses.contains(component.status) && component.group_id.nonEmpty
  }

  def transform(component: Component): CompactComponent = {
    CompactComponent(
      composite_id = s"${component.page_id}|${component.group_id}".hashCode.toString,
      name = component.name,
      status = component.status
    )
  }

  // this could error in a real world scenario and we should handle it gracefully
  def saveToDb(records: Array[CompactComponent]): Unit = {
    DB localTx { implicit session =>
      val params = records.map(c => Seq(c.composite_id, c.name, c.status)).toSeq
      sql"INSERT INTO records (composite_id, name, status) VALUES (?, ?, ?)".batch(params: _*).apply()
    }
  }

  def queryDigitalOcean(): Future[ComponentsResponse] = {
    Http()
      .singleRequest(HttpRequest(uri = "https://s2k7tnzlhrpw.statuspage.io/api/v1/components.json"))
      .flatMap { response =>
        Unmarshal(response).to[ComponentsResponse]
      }
  }

  val route =
    path("api" / "v1" / "components") {
      get {
        parameters('name.?) { names =>
          val nameSet = names.map(_.split(",").toSet).getOrElse(Set())
          logger.info(s"Querying for names: $nameSet")
          complete(queryDigitalOcean()
            .map { resp =>
              val validComponents =
                if (names.isEmpty) {
                  resp.components.filter(isValidComponent).map(transform)
                } else {
                  resp.components.filter(isValidComponent).filter(c => nameSet.contains(c.name)).map(transform)
                }
              saveToDb(validComponents)
              EndpointResponse(validComponents.length)
            }
          )
        }
      }
    }

  var binding: Option[ServerBinding] = Some(Await.result(Http().bindAndHandle(route, params.host, params.port), 10.seconds))

  def stop(): Unit = {
    binding match {
      case Some(b) =>
        Await.result(b.unbind(), 10.seconds)
        Await.result(system.terminate(), 10.seconds)
      case None =>
        logger.warn("trying to stop endpoint that was already stopped")
    }
  }

  override def close(): Unit = stop()
}
