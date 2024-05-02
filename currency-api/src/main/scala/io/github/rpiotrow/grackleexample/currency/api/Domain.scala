package io.github.rpiotrow.grackleexample.currency.api

object Domain:
  case class Currency(
    code: String,
    name: String,
    symbol: Option[String],
    decimalPlaces: Option[Int],
    currencyType: Option[CurrencyType],
    exchangeRates: Seq[ExchangeRate],
    issuingAuthority: String
  )

  enum CurrencyType:
    case FIAT, CRYPTO

  case class ExchangeRate(rate: Float, referenceCurrency: String)
