package io.github.rpiotrow.grackleexample.currency.api

import io.circe.generic.auto.*
import io.github.rpiotrow.grackleexample.currency.api.Domain.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

object Endpoints:
  val getCurrency: Endpoint[Unit, String, Unit, Option[Currency], Any] = endpoint.get
    .in("currency")
    .in(query[String]("code"))
    .out(jsonBody[Option[Currency]])
