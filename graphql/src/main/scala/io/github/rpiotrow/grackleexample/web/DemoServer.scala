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

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.comcast.ip4s.*
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.staticcontent.resourceServiceBuilder

object DemoServer:
  def run(graphQLRoutes: HttpRoutes[IO]): Resource[IO, Unit] =
    val httpApp = (
      // Routes for static resources, i.e. GraphQL Playground
      resourceServiceBuilder[IO]("/assets").toRoutes <+>
        // GraphQL routes
        graphQLRoutes
    ).orNotFound

    // Spin up the server ...
    EmberServerBuilder
      .default[IO]
      .withHost(ip"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build
      .void
