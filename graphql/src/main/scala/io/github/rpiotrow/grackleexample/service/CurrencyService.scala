package io.github.rpiotrow.grackleexample.service

import cats.effect.{Async, Resource}
import cats.implicits.given
import io.circe.Json
import sttp.client4.*
import sttp.client4.circe.*
import sttp.client4.httpclient.cats.HttpClientCatsBackend

trait CurrencyService[F[_]]:
  def getCurrencyInfo(code: String): F[Option[Json]]

object CurrencyService:

  def resource[F[_]: Async]: Resource[F, CurrencyService[F]] =
    HttpClientCatsBackend.resource[F]().map { backend => (code: String) =>
      basicRequest
        .get(uri"http://localhost:9090/currency?code=$code")
        .response(asJson[Json])
        .send(backend)
        .map(_.body.toOption)
    }
