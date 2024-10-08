package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import co.topl.brambl.dataApi.{GenusQueryAlgebra, RpcChannelResource}
import scopt.OParser
import xyz.stratalab.strata.cli.controllers.GenusQueryController
import xyz.stratalab.strata.cli.{StrataCliParams, StrataCliParamsParserModule, StrataCliSubCmd}

trait GenusQueryModeModule extends WalletStateAlgebraModule with RpcChannelResource {

  def genusQuerySubcmd(
    validateParams: StrataCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case StrataCliSubCmd.invalid =>
      IO.pure(
        Left(
          OParser.usage(
            StrataCliParamsParserModule.genusQueryMode
          ) + "\nA subcommand needs to be specified"
        )
      )
    case StrataCliSubCmd.utxobyaddress =>
      new GenusQueryController(
        walletStateAlgebra(
          validateParams.walletFile
        ),
        GenusQueryAlgebra
          .make[IO](
            channelResource(
              validateParams.host,
              validateParams.bifrostPort,
              validateParams.secureConnection
            )
          )
      ).queryUtxoFromParams(
        validateParams.fromAddress,
        validateParams.fromFellowship,
        validateParams.fromTemplate,
        validateParams.someFromInteraction,
        validateParams.tokenType
      )
  }

}
