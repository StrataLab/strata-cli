package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import co.topl.brambl.dataApi.{BifrostQueryAlgebra, RpcChannelResource}
import scopt.OParser
import xyz.stratalab.strata.cli.controllers.BifrostQueryController
import xyz.stratalab.strata.cli.{StrataCliParams, StrataCliParamsParserModule, StrataCliSubCmd}

trait BifrostQueryModeModule extends RpcChannelResource {

  def bifrostQuerySubcmd(
    validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val bifrostQueryAlgebra = BifrostQueryAlgebra.make[IO](
      channelResource(
        validateParams.host,
        validateParams.bifrostPort,
        validateParams.secureConnection
      )
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.bifrostQueryMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.mintblock =>
        new BifrostQueryController(bifrostQueryAlgebra)
          .makeBlock(
            validateParams.nbOfBlocks
          )
      case StrataCliSubCmd.blockbyheight =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).blockByHeight(validateParams.height)
      case StrataCliSubCmd.blockbyid =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).blockById(validateParams.blockId)
      case StrataCliSubCmd.transactionbyid =>
        new BifrostQueryController(
          bifrostQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
