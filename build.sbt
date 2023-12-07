val scala3Version = "3.3.1"

val grackleVersion   = "0.17.0"
val log4catsVersion  = "2.6.0"
val logbackVersion   = "1.4.14"
val doobieVersion    = "1.0.0-RC5"
val http4sVersion    = "0.23.24"
val flywayVersion    = "10.1.0"
val whaleTailVersion = "0.0.10"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "grackle-example",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
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
