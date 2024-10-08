package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import co.topl.brambl.constants.NetworkConstants
import scopt.OParser
import xyz.stratalab.strata.cli.controllers.SimpleMintingController
import xyz.stratalab.strata.cli.impl.{AssetStatementParserModule, GroupPolicyParserModule, SeriesPolicyParserModule}
import xyz.stratalab.strata.cli.{StrataCliParams, StrataCliParamsParserModule, StrataCliSubCmd, TokenType}

trait SimpleMintingModeModule
    extends GroupPolicyParserModule
    with SeriesPolicyParserModule
    with AssetStatementParserModule
    with SimpleMintingAlgebraModule {

  def simpleMintingSubcmds(
    validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val simpleMintingController = new SimpleMintingController(
      groupPolicyParserAlgebra(validateParams.network.networkId),
      seriesPolicyParserAlgebra(validateParams.network.networkId),
      assetMintingStatementParserAlgebra(validateParams.network.networkId),
      simpleMintingAlgebra(
        validateParams.walletFile,
        validateParams.network.networkId,
        NetworkConstants.MAIN_LEDGER_ID,
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
              StrataCliParamsParserModule.simpleMintingMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.create =>
        validateParams.tokenType match {
          case TokenType.group =>
            simpleMintingController
              .createSimpleGroupMintingTransactionFromParams(
                validateParams.someInputFile.get,
                validateParams.someKeyFile.get,
                validateParams.password,
                validateParams.fromFellowship,
                validateParams.fromTemplate,
                validateParams.someFromInteraction,
                validateParams.amount,
                validateParams.fee,
                validateParams.someOutputFile.get
              )
          case TokenType.series =>
            simpleMintingController
              .createSimpleSeriesMintingTransactionFromParams(
                validateParams.someInputFile.get,
                validateParams.someKeyFile.get,
                validateParams.password,
                validateParams.fromFellowship,
                validateParams.fromTemplate,
                validateParams.someFromInteraction,
                validateParams.amount,
                validateParams.fee,
                validateParams.someOutputFile.get
              )
          case TokenType.asset =>
            simpleMintingController
              .createSimpleAssetMintingTransactionFromParams(
                validateParams.someInputFile.get,
                validateParams.someKeyFile.get,
                validateParams.password,
                validateParams.fromFellowship,
                validateParams.fromTemplate,
                validateParams.someFromInteraction,
                validateParams.fee,
                validateParams.ephemeralMetadata,
                validateParams.someCommitment,
                validateParams.someOutputFile.get
              )
        }
    }
  }
}
