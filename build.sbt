val scala3Version = "3.3.1"

val grackleVersion   = "0.17.0"
val log4catsVersion  = "2.6.0"
val logbackVersion   = "1.4.14"
val doobieVersion    = "1.0.0-RC5"
val http4sVersion    = "0.23.24"
val flywayVersion    = "10.1.0"
val whaleTailVersion = "0.0.10"

lazy val commonSettings = Seq(
  scalaVersion := scala3Version,
  version := "0.0.1-SNAPSHOT",
  organization := "io.github.rpiotrow",
  run / fork := true,
  publish / skip := true
)

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings*)
    .aggregate(
      `currency-api`,
      `currency-server`,
      `local-database`,
      graphql
    )

lazy val `currency-api` = project.in(file("currency-api")).settings(commonSettings*)

lazy val `currency-server` = project.in(file("currency-server")).settings(commonSettings*).dependsOn(`currency-api`)

lazy val `local-database` = project
  .settings(commonSettings*)
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat"      %% "doobie-hikari"              % doobieVersion,
      "org.flywaydb"       % "flyway-database-postgresql" % flywayVersion,
      "io.chrisdavenport" %% "whale-tail-manager"         % whaleTailVersion
    )
  )
  .in(file("local-database"))

lazy val graphql = project
  .in(file("graphql"))
  .settings(commonSettings*)
  .settings(
    name         := "grackle-example",
    libraryDependencies ++= Seq(
      "org.typelevel"     %% "grackle-core"               % grackleVersion,
      "org.typelevel"     %% "grackle-circe"              % grackleVersion,
      "org.typelevel"     %% "grackle-generic"            % grackleVersion,
      "org.typelevel"     %% "grackle-doobie-pg"          % grackleVersion,
      "org.typelevel"     %% "log4cats-slf4j"             % log4catsVersion,
      "ch.qos.logback"     % "logback-classic"            % logbackVersion,
      "org.tpolecat"      %% "doobie-postgres"            % doobieVersion,
      "org.tpolecat"      %% "doobie-hikari"              % doobieVersion,
      "org.http4s"        %% "http4s-ember-server"        % http4sVersion,
      "org.http4s"        %% "http4s-ember-client"        % http4sVersion,
      "org.http4s"        %% "http4s-circe"               % http4sVersion,
      "org.http4s"        %% "http4s-dsl"                 % http4sVersion,
      "org.flywaydb"       % "flyway-database-postgresql" % flywayVersion,
      "io.chrisdavenport" %% "whale-tail-manager"         % whaleTailVersion
    )
  )
  .dependsOn(`currency-server`, `local-database`)
