package io.scalac.common

import monix.eval.Task
import org.scalatest.{Matchers, WordSpec}
import io.scalac.common.logger.AppLogger
import io.scalac.common.services.{Context, Logger, NoopServiceProfiler, Profiler, Service, ServiceError}
import _root_.play.api.libs.json._

trait BaseUnitTest extends WordSpec with Matchers with FuturesSupport {

  implicit val scheduler = monix.execution.Scheduler.Implicits.global
  implicit val serviceContext: auth.ServiceContext = auth.EmptyContext()
  implicit val serviceProfiler: services.ServiceProfiler = NoopServiceProfiler
  implicit val logger: AppLogger = AppLogger(getClass)

  def mockService[Req, Res, E <: ServiceError](fn: Req => Task[Either[E, Res]]): Service[Req, Res, E] = new Service[Req, Res, E] {
    def apply[Ctx: Context, P: Profiler, L: Logger](req: Req): Task[Either[E, Res]] = fn(req)
  }

  implicit class JsObjectDowncast(jsValue: JsValue) {
    def asJsObject: JsObject = jsValue match {
      case obj: JsObject => obj
      case _ => fail("value expected to be JsObject")
    }

    def asJsArray: JsArray = jsValue match {
      case arr: JsArray => arr
      case _ => fail("value expected to be JsArray")
    }
  }
}
