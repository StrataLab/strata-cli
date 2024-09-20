package xyz.stratalab.brambl.cli.modules

import cats.effect.IO
import xyz.stratalab.brambl.cli.BramblCliSubCmd
import xyz.stratalab.brambl.cli.controllers.SimpleTransactionController
import xyz.stratalab.brambl.cli.BramblCliParams
import scopt.OParser
import xyz.stratalab.brambl.cli.BramblCliParamsParserModule

trait SimpleTransactionModeModule
    extends SimpleTransactionAlgebraModule
    with WalletStateAlgebraModule {

  def simpleTransactionSubcmds(
      validateParams: BramblCliParams
  ): IO[Either[String, String]] = validateParams.subcmd match {
    case BramblCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              BramblCliParamsParserModule.simpleTransactionMode
            ) + "\nA subcommand needs to be specified"
          )
        )
    case BramblCliSubCmd.create =>
      new SimpleTransactionController(
        walletStateAlgebra(
          validateParams.walletFile
        ),
        simplTransactionOps(
          validateParams.walletFile,
          validateParams.network.networkId,
          validateParams.host,
          validateParams.bifrostPort,
          validateParams.secureConnection
        )
      ).createSimpleTransactionFromParams(
        validateParams.someKeyFile.get,
        validateParams.password,
        (
          validateParams.fromFellowship,
          validateParams.fromTemplate,
          validateParams.someFromInteraction
        ),
        (
          validateParams.someChangeFellowship,
          validateParams.someChangeTemplate,
          validateParams.someChangeInteraction
        ),
        validateParams.toAddress,
        validateParams.someToFellowship,
        validateParams.someToTemplate,
        validateParams.amount,
        validateParams.fee,
        validateParams.someOutputFile.get,
        validateParams.tokenType,
        validateParams.someGroupId,
        validateParams.someSeriesId
      )
  }
}
