package endpoint

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import models.EndpointResponse
import models.digital_ocean.{Component, ComponentsResponse, Page}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

// implicits required for serializing models used by the endpoint
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val pageFormats = jsonFormat3(Page)

  implicit object ZonedDateTimeJsonFormat extends RootJsonFormat[ZonedDateTime] {
    val formatter = DateTimeFormatter.ISO_DATE_TIME

    override def read(json: JsValue): ZonedDateTime = {
      json match {
        case JsString(s) =>
          ZonedDateTime.from(formatter.parse(s))
      }
    }

    override def write(obj: ZonedDateTime): JsValue = {
      JsString(obj.format(formatter))
    }
  }

  implicit val componentsFormats = jsonFormat10(Component)
  implicit val componentsResponseFormats = jsonFormat2(ComponentsResponse)
  implicit val endpointResponseFormats = jsonFormat1(EndpointResponse)
}
