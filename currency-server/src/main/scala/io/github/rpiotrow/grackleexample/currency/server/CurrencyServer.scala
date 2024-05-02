package io.github.rpiotrow.grackleexample.currency.server

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.comcast.ip4s.*
import io.github.rpiotrow.grackleexample.currency.api.Domain.*
import io.github.rpiotrow.grackleexample.currency.api.Endpoints
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter

object CurrencyServer:
  def run(): Resource[IO, Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ip"0.0.0.0")
      .withPort(port"9090")
      .withHttpApp(getCurrencyRoute.orNotFound)
      .build
      .void

  private val getCurrencyRoute: HttpRoutes[IO] =
    Http4sServerInterpreter[IO]().toRoutes(Endpoints.getCurrency.serverLogic(getCurrency))

  private def getCurrency(code: String): IO[Either[Unit, Option[Currency]]] =
    IO.pure(Right(data.get(code)))

  private val data: Map[String, Currency] = Map(
    "PLN" -> Currency(
      "PLN",
      "Złoty",
      Some("zł"),
      Some(2),
      Some(CurrencyType.FIAT),
      List(ExchangeRate(1.0, "PLN")),
      "Narodowy Bank Polski"
    ),
    "USD" -> Currency(
      "USD",
      "US Dollar",
      Some("$"),
      Some(2),
      Some(CurrencyType.FIAT),
      List(ExchangeRate(4.02, "PLN")),
      "Federal Reserve System"
    ),
    "CAD" -> Currency(
      "CAD",
      "Canadian Dollar",
      Some("$"),
      Some(2),
      Some(CurrencyType.FIAT),
      List(ExchangeRate(1.0, "CAD")),
      "Bank of Canada"
    ),
    "GBP" -> Currency(
      "GBP",
      "Pound Sterling",
      Some("£"),
      Some(2),
      Some(CurrencyType.FIAT),
      List(ExchangeRate(5.05, "PLN")),
      "Bank of England"
    ),
    "AUD" -> Currency(
      "AUD",
      "Australian Dollar",
      Some("$"),
      Some(2),
      Some(CurrencyType.FIAT),
      List(ExchangeRate(2.64, "PLN")),
      "Reserve Bank of Australia"
    ),
    "JPY" -> Currency(
      "JPY",
      "Japanese Yen",
      Some("¥"),
      Some(4),
      Some(CurrencyType.FIAT),
      List(ExchangeRate(0.028, "JPY")),
      "Bank of Japan"
    )
  )
