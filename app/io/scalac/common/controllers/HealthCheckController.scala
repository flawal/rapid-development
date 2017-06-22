package io.scalac.common.controllers

import javax.inject.Singleton

import com.google.inject.Inject
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Controller}

import io.scalac.common.auth
import io.scalac.common.core.Correlation
import io.scalac.common.logger.Logging
import io.scalac.common.services.{ExternalHealthCheckResponse, HealthCheckRequest, HealthCheckResponse, _}

@Singleton
class HealthCheckController @Inject() (
  healthCheckServices: HealthCheckServices
)(implicit val profiler: ServiceProfiler)
  extends Controller
    with Logging {

  //TODO inject
  import monix.execution.Scheduler.Implicits.global

  implicit val externalHealthCheckWrites = Json.writes[ExternalHealthCheckResponse]
  implicit val healthCheckWrites = Json.writes[HealthCheckResponse]

  def healthCheck(diagnostics: Boolean): Action[AnyContent] = Action.async { request =>
    implicit val emptyContext = auth.EmptyContext()
    implicit val cid = Correlation.getCorrelation(request.headers.toSimpleMap)
    logger.info(s"${request.path} - getting app status")
    val future = healthCheckServices.healthCheck(HealthCheckRequest(diagnostics)).runAsync
    future.map { either_ =>
      either_.fold(
        serviceError => Ok(s"Failed due to: $serviceError"),
        response => Ok(Json.prettyPrint(Json.toJson(response)))
      )
    }
  }
}