package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import co.topl.brambl.dataApi.{GenusQueryAlgebra, RpcChannelResource}

trait GenusQueryAlgebraModule extends RpcChannelResource {

  def genusQueryAlgebra(host: String, port: Int, secureConnection: Boolean) =
    GenusQueryAlgebra.make[IO](
      channelResource(
        host,
        port,
        secureConnection
      )
    )
}
