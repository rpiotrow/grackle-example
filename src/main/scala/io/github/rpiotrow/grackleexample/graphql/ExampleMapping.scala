package io.github.rpiotrow.grackleexample.graphql

import _root_.doobie.{Meta, Transactor}
import cats.effect.Sync
import cats.implicits.*
import grackle.*
import grackle.Predicate.*
import grackle.Query.*
import grackle.QueryCompiler.*
import grackle.Value.*
import grackle.doobie.postgres.{DoobieMapping, DoobieMonitor, LoggedDoobieMappingCompanion}
import grackle.syntax.*
import io.circe.Json
import io.github.rpiotrow.grackleexample.service.CurrencyService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

trait ExampleMapping[F[_]] extends DoobieMapping[F]:

  override val schema =
    schema"""
      type Query {
        country(code: String!): Country
      }
      type Country {
        code: String!
        name: String!
        currency: Currency
      }
      type Currency {
        code: String!
        name: String!
        symbol: String
        decimalPlaces: Int
        currencyType: CurrencyType
        exchangeRates: [ExchangeRate]
        issuingAuthority: String
      }
      enum CurrencyType {
        FIAT
        CRYPTO
      }
      type ExchangeRate {
        rate: Float!
        referenceCurrency: String!
      }
    """

  private val QueryType = schema.ref("Query")
  private val CountryType = schema.ref("Country")
  private val CurrencyType = schema.ref("Currency")
  private val CurrencyTypeType = schema.ref("CurrencyType")
  private val ExchangeRateType = schema.ref("ExchangeRate")
  private val currencyService: CurrencyService[F] = CurrencyService.impl[F]

  private object country extends TableDef("country"):
    val code: ColumnRef = col("code", Meta[String])
    val name: ColumnRef = col("name", Meta[String])
    val currencyCode: ColumnRef = col("currency_code", Meta[String])

  override val typeMappings: List[TypeMapping] =
    List(
      ObjectMapping(tpe = QueryType, fieldMappings = List(SqlObject("country"))),
      ObjectMapping(
        tpe = CountryType,
        fieldMappings = List(
          SqlField("code", country.code, key = true),
          SqlField("name", country.name),
          SqlField("currency_code", country.currencyCode, hidden = true),
          EffectField("currency", CurrencyQueryHandler, List("currency_code"))
        )
      )
    )
  override val selectElaborator: SelectElaborator = SelectElaborator {
    case (QueryType, "country", List(Binding("code", StringValue(code)))) =>
      Elab.transformChild { child =>
        Unique(Filter(Eql(CountryType / "code", Const(code)), child))
      }
  }

  private object CurrencyQueryHandler extends ExternalFetcher:
    val fieldName = "currency_code"
    def fetch(value: String): F[Option[Json]] = currencyService.getCurrencyInfo(value)

  private trait ExternalFetcher extends EffectHandler[F]:
    def fieldName: String
    def fetch(value: String): F[Option[Json]]

    override def runEffects(queries: List[(Query, Cursor)]): F[Result[List[Cursor]]] =
      queries
        .traverse { case (query, cursor) =>
          cursor
            .fieldAs[String](fieldName)
            .toOption
            .map(fetch)
            .sequence
            .map(_.flatten.getOrElse(Json.Null))
            .map { json =>
              Query.childContext(cursor.context, query).map { context =>
                CirceCursor(context, json, Some(cursor), Env.empty)
              }
            }
        }
        .map(_.sequence)

object ExampleMapping extends LoggedDoobieMappingCompanion:

  def mkMapping[F[_]: Sync](transactor: Transactor[F], monitor: DoobieMonitor[F]): ExampleMapping[F] =
    new DoobieMapping(transactor, monitor) with ExampleMapping[F]

  def mkMappingFromTransactor[F[_]: Sync](transactor: Transactor[F]): Mapping[F] =
    implicit val logger: Logger[F] = Slf4jLogger.getLoggerFromName[F]("SqlQueryLogger")
    mkMapping(transactor)
