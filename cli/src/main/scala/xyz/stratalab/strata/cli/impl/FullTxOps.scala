package xyz.stratalab.strata.cli.impl

import cats.data.EitherT
import cats.effect.IO
import xyz.stratalab.strata.cli.NetworkIdentifiers
import xyz.stratalab.strata.cli.TokenType
import xyz.stratalab.strata.cli.controllers.SimpleTransactionController
import xyz.stratalab.strata.cli.controllers.TxController
import xyz.stratalab.strata.cli.modules.SimpleTransactionModeModule
import xyz.stratalab.strata.cli.modules.TxModeModule
import xyz.stratalab.strata.cli.modules.WalletModeModule
import xyz.stratalab.sdk.constants.NetworkConstants
import xyz.stratalab.sdk.models.LockAddress
import xyz.stratalab.sdk.models.GroupId
import xyz.stratalab.sdk.utils.Encoding
import com.google.protobuf.ByteString
import xyz.stratalab.sdk.models.SeriesId

object FullTxOps
    extends WalletModeModule
    with SimpleTransactionModeModule
    with TxModeModule {

  private def selectToken(token: String) =
    if (token == "LVL")
      TokenType.lvl
    else if (token.startsWith(":"))
      TokenType.series
    else if (token.endsWith(":"))
      TokenType.group
    else
      TokenType.asset

  private def selectGroupId(token: String): Option[GroupId] =
    if (token.endsWith(":"))
      Some(
        GroupId(
          ByteString.copyFrom(
            Encoding.decodeFromHex(token.dropRight(1)).toOption.get
          )
        )
      )
    else if (token.startsWith(":"))
      None
    else if (token == "LVL")
      None
    else
      Some(
        GroupId(
          ByteString.copyFrom(
            Encoding.decodeFromHex(token.split(":").head).toOption.get
          )
        )
      )

  def selectSeriesId(token: String): Option[SeriesId] =
    if (token.startsWith(":"))
      Some(
        SeriesId(
          ByteString.copyFrom(
            Encoding.decodeFromHex(token.drop(1)).toOption.get
          )
        )
      )
    else if (token.endsWith(":"))
      None
    else if (token == "LVL")
      None
    else
      Some(
        SeriesId(
          ByteString.copyFrom(
            Encoding.decodeFromHex(token.split(":").last).toOption.get
          )
        )
      )

  def sendFunds(
      networkId: NetworkIdentifiers,
      password: String,
      walletFile: String,
      keyFile: String,
      fromFellowship: String,
      fromTemplate: String,
      someFromInteraction: Option[Int],
      someChangeFellowship: Option[String],
      someChangeTemplate: Option[String],
      someChangeInteraction: Option[Int],
      toAddress: Option[LockAddress],
      amount: Long,
      fee: Long,
      token: String,
      txFile: String,
      provedTxFile: String,
      host: String,
      bifrostPort: Int,
      secureConnection: Boolean
  ): IO[Either[String, String]] = {
    val simpleTxController = new SimpleTransactionController(
      walletStateAlgebra(
        walletFile
      ),
      simplTransactionOps(
        walletFile,
        networkId.networkId,
        host,
        bifrostPort,
        secureConnection
      )
    )

    val txController = new TxController(
      txParserAlgebra(
        networkId.networkId,
        NetworkConstants.MAIN_LEDGER_ID
      ),
      transactionOps(
        walletFile,
        host,
        bifrostPort,
        secureConnection
      )
    )

    (for {
      _ <- EitherT(
        simpleTxController.createSimpleTransactionFromParams(
          keyFile,
          password,
          (
            fromFellowship,
            fromTemplate,
            someFromInteraction
          ),
          (
            someChangeFellowship,
            someChangeTemplate,
            someChangeInteraction
          ),
          toAddress,
          None,
          None,
          amount,
          fee,
          txFile,
          selectToken(token),
          selectGroupId(token),
          selectSeriesId(token)
        )
      )
      _ <- EitherT(
        txController.proveSimpleTransactionFromParams(
          txFile,
          keyFile,
          password,
          provedTxFile
        )
      )
      res <- EitherT(
        txController.broadcastSimpleTransactionFromParams(
          provedTxFile
        )
      )
    } yield "Transaction id: " + res).value.map {
      case Left(err) =>
        println(err)
        Left(err)
      case Right(res) => Right(res)
    }
  }

}
