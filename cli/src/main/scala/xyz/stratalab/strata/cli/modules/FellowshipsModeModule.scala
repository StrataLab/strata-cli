package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import co.topl.brambl.servicekit.{FellowshipStorageApi, WalletStateResource}
import scopt.OParser
import xyz.stratalab.strata.cli.controllers.FellowshipsController
import xyz.stratalab.strata.cli.{StrataCliParams, StrataCliParamsParserModule, StrataCliSubCmd}

trait FellowshipsModeModule extends WalletStateResource {

  def fellowshipsModeSubcmds(
    validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val fellowshipStorageAlgebra = FellowshipStorageApi.make[IO](
      walletResource(validateParams.walletFile)
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.fellowshipsMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.add =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .addFellowship(validateParams.fellowshipName)
      case StrataCliSubCmd.list =>
        new FellowshipsController(fellowshipStorageAlgebra)
          .listFellowships()
    }
  }
}
