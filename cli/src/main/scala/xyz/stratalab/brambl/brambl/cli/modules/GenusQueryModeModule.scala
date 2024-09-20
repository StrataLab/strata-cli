package xyz.stratalab.brambl.cli.modules

import cats.effect.IO
import xyz.stratalab.brambl.cli.controllers.GenusQueryController
import co.topl.brambl.dataApi.{GenusQueryAlgebra, RpcChannelResource}
import xyz.stratalab.brambl.cli.BramblCliSubCmd
import xyz.stratalab.brambl.cli.BramblCliParams
import scopt.OParser
import xyz.stratalab.brambl.cli.BramblCliParamsParserModule

trait GenusQueryModeModule
    extends WalletStateAlgebraModule
    with RpcChannelResource {

  def genusQuerySubcmd(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case BramblCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              BramblCliParamsParserModule.genusQueryMode
            ) + "\nA subcommand needs to be specified"
          )
        )
    case BramblCliSubCmd.utxobyaddress =>
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
