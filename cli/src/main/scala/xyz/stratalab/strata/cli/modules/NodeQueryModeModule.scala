package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.controllers.NodeQueryController
import xyz.stratalab.strata.cli.StrataCliSubCmd
import xyz.stratalab.sdk.dataApi.{NodeQueryAlgebra, RpcChannelResource}
import xyz.stratalab.strata.cli.StrataCliParams
import scopt.OParser
import xyz.stratalab.strata.cli.StrataCliParamsParserModule

trait NodeQueryModeModule extends RpcChannelResource {

  def bifrostQuerySubcmd(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val bifrostQueryAlgebra = NodeQueryAlgebra.make[IO](
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
        new NodeQueryController(bifrostQueryAlgebra)
          .makeBlock(
            validateParams.nbOfBlocks
          )
      case StrataCliSubCmd.blockbyheight =>
        new NodeQueryController(
          bifrostQueryAlgebra
        ).blockByHeight(validateParams.height)
      case StrataCliSubCmd.blockbyid =>
        new NodeQueryController(
          bifrostQueryAlgebra
        ).blockById(validateParams.blockId)
      case StrataCliSubCmd.transactionbyid =>
        new NodeQueryController(
          bifrostQueryAlgebra
        ).fetchTransaction(validateParams.transactionId)
    }
  }

}
