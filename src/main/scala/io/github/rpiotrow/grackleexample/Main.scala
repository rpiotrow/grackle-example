package io.github.rpiotrow.grackleexample

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all.*
import doobie.hikari.HikariTransactor
import io.chrisdavenport.whaletail.Docker
import io.chrisdavenport.whaletail.manager.*
import io.github.rpiotrow.grackleexample.graphql.ExampleMapping
import io.github.rpiotrow.grackleexample.web.{DemoServer, GraphQLService}
import org.flywaydb.core.Flyway

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

object Main extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    DBSetup.run { xa =>
      DemoServer.resource(GraphQLService.routes("api", GraphQLService.fromMapping(ExampleMapping.mkMappingFromTransactor(xa))))
    }

object DBSetup:
  private val container: Resource[IO, PostgresConnectionInfo] = Docker
    .default[IO]
    .flatMap(client =>
      WhaleTailContainer
        .build(
          client,
          image = "postgres",
          tag = "11.8".some,
          ports = Map(PostgresConnectionInfo.DefaultPort -> None),
          env = Map("POSTGRES_USER" -> "test", "POSTGRES_PASSWORD" -> "test", "POSTGRES_DB" -> "test"),
          labels = Map.empty
        )
        .evalTap(
          ReadinessStrategy.checkReadiness(
            client,
            _,
            ReadinessStrategy.LogRegex(".*database system is ready to accept connections.*".r, 2),
            30.seconds
          )
        )
    )
    .flatMap(container =>
      Resource.eval(container.ports.get(PostgresConnectionInfo.DefaultPort).liftTo[IO](new Throwable("Missing Port")))
    )
    .map { case (host, port) =>
      PostgresConnectionInfo(host, port)
    }

  def run(body: HikariTransactor[IO] => Resource[IO, Unit]): IO[Nothing] =
    container.evalTap(dbMigration).flatMap(transactor).flatMap(body).useForever

  private def transactor(connInfo: PostgresConnectionInfo): Resource[IO, HikariTransactor[IO]] =
    import connInfo.*
    HikariTransactor.newHikariTransactor[IO](
      driverClassName,
      jdbcUrl,
      username,
      password,
      ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
    )

  private def dbMigration(connInfo: PostgresConnectionInfo): IO[Unit] =
    import connInfo.*
    IO.blocking {
      val flyway = Flyway
        .configure()
        .dataSource(jdbcUrl, username, password)
      flyway.load().migrate()
    }.void

  private case class PostgresConnectionInfo(host: String, port: Int):
    val driverClassName = "org.postgresql.Driver"
    val jdbcUrl         = s"jdbc:postgresql://$host:$port/test"
    val username        = "test"
    val password        = "test"

  private object PostgresConnectionInfo:
    val DefaultPort = 5432
