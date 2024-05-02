package io.github.rpiotrow.grackleexample

import cats.effect.{ExitCode, IO, IOApp}
import io.github.rpiotrow.grackleexample.currency.server.CurrencyServer
import io.github.rpiotrow.grackleexample.db.LocalDatabase
import io.github.rpiotrow.grackleexample.graphql.ExampleMapping
import io.github.rpiotrow.grackleexample.service.CurrencyService
import io.github.rpiotrow.grackleexample.web.{DemoServer, GraphQLService}

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    LocalDatabase.run { xa =>
      for
        _               <- CurrencyServer.run()
        currencyService <- CurrencyService.resource[IO]
        graphqlService = GraphQLService.fromMapping(ExampleMapping.mkMappingFromTransactor(xa, currencyService))
        _ <- DemoServer.run(GraphQLService.routes("api", graphqlService))
      yield ()
    }
