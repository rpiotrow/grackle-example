package io.github.rpiotrow.grackleexample.service

import cats.effect.Sync
import cats.implicits.given
import io.circe.Json
import io.circe.parser.parse

trait CurrencyService[F[_]]:
  def getCurrencyInfo(code: String): F[Option[Json]]

object CurrencyService:
  def impl[F[_]: Sync]: CurrencyService[F] = (code: String) =>
    rawData.get(code).flatMap(s => parse(s).toOption).pure[F]

  private val rawData: Map[String, String] = Map(
    "PLN" -> """{
               |  "code": "PLN",
               |  "name": "Złoty",
               |  "symbol": "zł",
               |  "decimalPlaces": 2,
               |  "currencyType": "FIAT",
               |  "exchangeRates": [
               |    {
               |      "rate": 1.0,
               |      "referenceCurrency": "PLN"
               |    }
               |  ],
               |  "issuingAuthority": "Narodowy Bank Polski"
               |}""".stripMargin,
    "USD" -> """{
               |  "code": "USD",
               |  "name": "US Dollar",
               |  "symbol": "$",
               |  "decimalPlaces": 2,
               |  "currencyType": "FIAT",
               |  "exchangeRates": [
               |    {
               |      "rate": 4.02,
               |      "referenceCurrency": "PLN"
               |    }
               |  ],
               |  "issuingAuthority": "Federal Reserve System"
               |  }""".stripMargin,
    "CAD" -> """{
               |  "code": "CAD",
               |  "name": "Canadian Dollar",
               |  "symbol": "$",
               |  "decimalPlaces": 2,
               |  "currencyType": "FIAT",
               |  "exchangeRates": [
               |    {
               |      "rate": 1.0,
               |      "referenceCurrency": "CAD"
               |    }
               |  ],
               |  "issuingAuthority": "Bank of Canada"
               |  }""".stripMargin,
    "GBP" -> """{
               |  "code": "GBP",
               |  "name": "Pound Sterling",
               |  "symbol": "£",
               |  "decimalPlaces": 2,
               |  "currencyType": "FIAT",
               |  "exchangeRates": [
               |    {
               |      "rate": 5.05,
               |      "referenceCurrency": "PLN"
               |    }
               |  ],
               |  "issuingAuthority": "Bank of England"
               |  }""".stripMargin,
    "AUD" -> """{
               |  "code": "AUD",
               |  "name": "Australian Dollar",
               |  "symbol": "$",
               |  "decimalPlaces": 2,
               |  "currencyType": "FIAT",
               |  "exchangeRates": [
               |    {
               |      "rate": 2.64,
               |      "referenceCurrency": "PLN"
               |    }
               |  ],
               |  "issuingAuthority": "Reserve Bank of Australia"
               |  }""".stripMargin,
    "JPY" -> """{
               |  "code": "JPY",
               |  "name": "Japanese Yen",
               |  "symbol": "¥",
               |  "decimalPlaces": 4,
               |  "currencyType": "FIAT",
               |  "exchangeRates": [
               |    {
               |      "rate": 0.028,
               |      "referenceCurrency": "JPY"
               |    }
               |  ],
               |  "issuingAuthority": "Bank of Japan"
               |  }""".stripMargin
  )
