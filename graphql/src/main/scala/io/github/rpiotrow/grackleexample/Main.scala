package io.github.rpiotrow.grackleexample

import cats.effect.{ExitCode, IO, IOApp}
import io.github.rpiotrow.grackleexample.db.LocalDatabase
import io.github.rpiotrow.grackleexample.graphql.ExampleMapping
import io.github.rpiotrow.grackleexample.web.{DemoServer, GraphQLService}

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    LocalDatabase.run { xa =>
      DemoServer.resource(GraphQLService.routes("api", GraphQLService.fromMapping(ExampleMapping.mkMappingFromTransactor(xa))))
    }
