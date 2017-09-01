package com.m5g

import com.google.common.collect.ImmutableList
import com.google.inject.Module
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.CommonFilters
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.logging.filter.{LoggingMDCFilter, TraceIdMDCFilter}

class DemoHttpService extends HttpServer {

  override def javaModules: java.util.Collection[Module] = ImmutableList.of[Module]()

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[CommonFilters]
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
  }
}

object DemoHttpServiceMain {
   def main(args: Array[String]) {
     new DemoHttpService().main(args)
   }
}
