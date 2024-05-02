// Copyright (c) 2016-2023 Association of Universities for Research in Astronomy, Inc. (AURA)
// Copyright (c) 2016-2023 Grackle Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.github.rpiotrow.grackleexample.web

import cats.effect.Concurrent
import cats.syntax.all.*
import grackle.Mapping
import io.circe.{Json, ParsingFailure, parser}
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, InvalidMessageBodyFailure, ParseFailure, QueryParamDecoder}

trait GraphQLService[F[_]]:
  def runQuery(op: Option[String], vars: Option[Json], query: String): F[Json]

object GraphQLService:

  def fromMapping[F[_]: Concurrent](mapping: Mapping[F]): GraphQLService[F] =
    (op: Option[String], vars: Option[Json], query: String) => mapping.compileAndRun(query, op, vars)

  def routes[F[_]: Concurrent](prefix: String, svc: GraphQLService[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    implicit val jsonQPDecoder: QueryParamDecoder[Json] =
      QueryParamDecoder[String].emap { s =>
        parser.parse(s).leftMap { case ParsingFailure(msg, _) =>
          ParseFailure("Invalid variables", msg)
        }
      }

    object QueryMatcher         extends QueryParamDecoderMatcher[String]("query")
    object OperationNameMatcher extends OptionalQueryParamDecoderMatcher[String]("operationName")
    object VariablesMatcher     extends OptionalValidatingQueryParamDecoderMatcher[Json]("variables")

    HttpRoutes.of[F] {
      case GET -> Root / `prefix` :?
          QueryMatcher(query) +& OperationNameMatcher(op) +& VariablesMatcher(vars0) =>
        vars0.sequence.fold(
          errors => BadRequest(errors.map(_.sanitized).mkString_("", ",", "")),
          vars =>
            for
              result <- svc.runQuery(op, vars, query)
              resp   <- Ok(result)
            yield resp
        )

      case req @ POST -> Root / `prefix` =>
        for
          body  <- req.as[Json]
          obj   <- body.asObject.liftTo[F](InvalidMessageBodyFailure("Invalid GraphQL query"))
          query <- obj("query").flatMap(_.asString).liftTo[F](InvalidMessageBodyFailure("Missing query field"))
          op   = obj("operationName").flatMap(_.asString)
          vars = obj("variables")
          result <- svc.runQuery(op, vars, query)
          resp   <- Ok(result)
        yield resp
    }
