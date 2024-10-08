package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.StrataCliParams
import xyz.stratalab.strata.cli.StrataCliSubCmd
import xyz.stratalab.strata.cli.controllers.WalletController
import xyz.stratalab.sdk.dataApi.{IndexerQueryAlgebra, RpcChannelResource}
import scopt.OParser
import xyz.stratalab.strata.cli.StrataCliParamsParserModule

trait WalletModeModule
    extends WalletStateAlgebraModule
    with WalletManagementUtilsModule
    with WalletApiModule
    with WalletAlgebraModule
    with TransactionBuilderApiModule
    with RpcChannelResource {

  def walletModeSubcmds(
      validateParams: StrataCliParams
  ): IO[Either[String, String]] = {
    val walletController = new WalletController(
      walletStateAlgebra(
        validateParams.walletFile
      ),
      walletManagementUtils,
      walletApi,
      walletAlgebra(
        validateParams.walletFile
      ),
      IndexerQueryAlgebra
        .make[IO](
          channelResource(
            validateParams.host,
            validateParams.bifrostPort,
            validateParams.secureConnection
          )
        )
    )
    validateParams.subcmd match {
      case StrataCliSubCmd.balance =>
        walletController.getBalance(
          validateParams.fromAddress,
          if (validateParams.fromAddress.isEmpty)
            Some(validateParams.fromFellowship)
          else None,
          if (validateParams.fromAddress.isEmpty)
            Some(validateParams.fromTemplate)
          else None,
          validateParams.someFromInteraction
        )
      case StrataCliSubCmd.addsecret =>
        walletController.addSecret(validateParams.secret, validateParams.digest)
      case StrataCliSubCmd.getpreimage =>
        walletController.getPreimage(
          validateParams.digest,
          validateParams.digestText
        )
      case StrataCliSubCmd.invalid =>
        IO.pure(
          Left(
            OParser.usage(
              StrataCliParamsParserModule.walletMode
            ) + "\nA subcommand needs to be specified"
          )
        )
      case StrataCliSubCmd.exportvk =>
        validateParams.someFromInteraction
          .map(x =>
            walletController.exportFinalVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.templateName,
              x
            )
          )
          .getOrElse(
            walletController.exportVk(
              validateParams.someKeyFile.get,
              validateParams.password,
              validateParams.someOutputFile.get,
              validateParams.fellowshipName,
              validateParams.templateName
            )
          )
      case StrataCliSubCmd.importvks =>
        walletController.importVk(
          validateParams.network.networkId,
          validateParams.inputVks,
          validateParams.someKeyFile.get,
          validateParams.password,
          validateParams.templateName,
          validateParams.fellowshipName
        )
      case StrataCliSubCmd.listinteraction =>
        walletController.listInteractions(
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case StrataCliSubCmd.init =>
        walletController.createWalletFromParams(validateParams)
      case StrataCliSubCmd.recoverkeys =>
        walletController.recoverKeysFromParams(validateParams)
      case StrataCliSubCmd.setinteraction =>
        walletController.setCurrentInteraction(
          validateParams.fromFellowship,
          validateParams.fromTemplate,
          validateParams.someFromInteraction.get
        )
      case StrataCliSubCmd.sync =>
        walletController.sync(
          validateParams.network.networkId,
          validateParams.fellowshipName,
          validateParams.templateName
        )
      case StrataCliSubCmd.currentaddress =>
        walletController.currentaddress(
          validateParams
        )
    }
  }
}
