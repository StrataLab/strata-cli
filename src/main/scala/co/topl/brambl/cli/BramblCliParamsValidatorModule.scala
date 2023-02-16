package co.topl.brambl.cli

import akka.http.scaladsl.model.Uri
import cats.data.Validated
import cats.data.ValidatedNel
import co.topl.client.Provider
import scala.util.Try
import co.topl.utils.StringDataTypes
import co.topl.utils.NetworkType

object BramblCliMode extends Enumeration {
  type BramblCliMode = Value

  val wallet, transaction = Value
}

object BramblCliSubCmd extends Enumeration {
  type BramblCliSubCmd = Value

  val create, sign, broadcast = Value
}


object TokenType extends Enumeration {
  type TokenType = Value

  val poly = Value
}

trait BramblCliParamsValidatorModule {

  object NetworkParamName extends Enumeration {
    type NetworkParamName = Value

    val main, valhalla, `private` = Value
  }

  def validateMode(mode: String) = {
    Try(BramblCliMode.withName(mode)).toOption match {
      case Some(mode) => Validated.validNel(mode)
      case None =>
        Validated.invalidNel(
          "Invalid mode. Valid values are " + BramblCliMode.values.mkString(
            ", "
          )
        )
    }
  }

  def validateSubCmd(
      mode: BramblCliMode.BramblCliMode,
      subcmd: String
  ): ValidatedNel[String, BramblCliSubCmd.Value] = {
    mode match {
      case BramblCliMode.wallet =>
        Try(BramblCliSubCmd.withName(subcmd)).toOption match {
          case Some(subcmd) =>
            val validSubCmds =
              List(BramblCliSubCmd.create, BramblCliSubCmd.sign)
            if (validSubCmds.contains(subcmd))
              Validated.validNel(subcmd)
            else
              Validated.invalidNel(
                "Invalid subcommand. Valid values are " + validSubCmds.mkString(
                  ", "
                )
              )
          case None =>
            Validated.invalidNel(
              "Invalid subcommand. Valid values are " + BramblCliSubCmd.values
                .mkString(", ")
            )
        }
      case BramblCliMode.transaction =>
        Try(BramblCliSubCmd.withName(subcmd)).toOption match {
          case Some(subcmd) =>
            val validSubCmds =
              List(BramblCliSubCmd.create, BramblCliSubCmd.sign)
            if (validSubCmds.contains(subcmd))
              Validated.validNel(subcmd)
            else
              Validated.invalidNel(
                "Invalid subcommand. Valid values are " + validSubCmds.mkString(
                  ", "
                )
              )
          case None =>
            Validated.invalidNel(
              "Invalid subcommand. Valid values are " + BramblCliSubCmd.values
                .mkString(", ")
            )
        }
    }
  }

  def validateNetworkType(networkType: String) = {
    Try(NetworkParamName.withName(networkType)).toOption match {
      case Some(networkType) => Validated.validNel(networkType)
      case None =>
        Validated.invalidNel(
          "Invalid network type. Valid values are main, valhalla, and private"
        )
    }
  }

  def buildNetwork(
      uri: Uri,
      networkType: NetworkParamName.Value,
      someApiKey: Option[String]
  ): Provider = {
    networkType match {
      case NetworkParamName.main =>
        new Provider.ToplMainNet(uri, someApiKey.getOrElse(""))
      case NetworkParamName.valhalla =>
        new Provider.ValhallaTestNet(uri, someApiKey.getOrElse(""))
      case NetworkParamName.`private` =>
        new Provider.PrivateTestNet(uri, someApiKey.getOrElse(""))
    }
  }
  def validateToplNetworkUri(networkUri: String): ValidatedNel[String, Uri] = {
    val invalidUriMessage = "Invalid Topl network URI"
    try {
      Uri(networkUri) match {
        case uri if uri.isAbsolute => Validated.validNel(uri)
        case _                     => Validated.invalidNel(invalidUriMessage)
      }
    } catch {
      case _: Throwable => Validated.invalidNel(invalidUriMessage)
    }
  }

  def validateWalletCreate(paramConfig: BramblCliParams) = {
    (paramConfig.somePassword) match {
      case Some(password)  =>
        Validated.validNel(password)
      case None =>
        Validated.invalidNel(
          "Password is required for wallet creation"
        )
    }
  }

  // def validateKeyfileAndPassword(someKeyFile: Option[String], somePassword: Option[String]) = {
  //   (someKeyFile, somePassword) match {
  //     case (Some(keyFile), Some(password)) =>
  //       Validated.validNel((keyFile, password))
  //     case (Some(keyFile), None) =>
  //       Validated.invalidNel(
  //         "Password is required "
  //       )
  //     case (None, Some(password)) =>
  //       Validated.invalidNel(
  //         "Keyfile is required for wallet creation"
  //       )
  //     case (None, None) =>
  //       Validated.invalidNel(
  //         "Keyfile and password are required for wallet creation"
  //       )
  //   }
  // }

  def validateFromAddresses(fromAddresses: Seq[String])(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    if (fromAddresses.isEmpty)
      Validated.invalidNel(
        "At least one from address is required"
      )
    else {
      import cats.implicits._
      fromAddresses.map(x => validateAddress(x)).sequence
    }
  }

  def validateToAddresses(toAddresses: Map[String, Int])(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    if (toAddresses.isEmpty)
      Validated.invalidNel(
        "At least one to address is required"
      )
    else {
      import cats.implicits._
      toAddresses.map(x => validateAddress(x._1).map(y => (y, x._2))).toList.sequence
    }
  }

  def validateAddress(address: String)(implicit networkPrefix: NetworkType.NetworkPrefix) = {
    import co.topl.attestation.AddressCodec.implicits._
    import co.topl.utils.IdiomaticScalaTransition.implicits.toValidatedOps
    Try(StringDataTypes.Base58Data.unsafe(address).decodeAddress.getOrThrow()).toOption match {
      case Some(_) => Validated.validNel(address)
      case None => Validated.invalidNel("Invalid address: " + address)
    }
  }

  def validateFee(fee: Int) = {
    if (fee < 0)
      Validated.invalidNel(
        "Fee must be greater than or equal to 0"
      )
    else
      Validated.validNel(fee)
  }

  def validateTokenType(token: String) = {
    Try(TokenType.withName(token)).toOption match {
      case Some(token) => Validated.validNel(token)
      case None =>
        Validated.invalidNel(
          "Invalid token type. Valid values are poly"
        )
    }
  }

  def validatTransactionCreate(paramConfig: BramblCliParams)(implicit networkPrefix: NetworkType.NetworkPrefix) = {
      (
        validateToplNetworkUri(paramConfig.someNetworkUri.getOrElse("")),
        validateTokenType(paramConfig.someToken.getOrElse("")),
        validateFromAddresses(paramConfig.fromAddresses),
        validateToAddresses(paramConfig.toAddresses),
        validateAddress(paramConfig.changeAddress),
        validateFee(paramConfig.fee)
      )
  }

  def validateParams(
      paramConfig: BramblCliParams
  ): ValidatedNel[String, BramblCliValidatedParams] = {
    import cats.implicits._
    (
      validateMode(paramConfig.mode).andThen(mode =>
        validateSubCmd(mode, paramConfig.subcmd).map((mode, _))
      ),
      (
        validateToplNetworkUri(paramConfig.someNetworkUri.getOrElse("http://127.0.0.1:9085")),
        validateNetworkType(paramConfig.networkType)
      ).mapN((uri, networkType) =>
        buildNetwork(
          uri,
          networkType,
          paramConfig.someApiKey
        )
      )
    ).mapN((modeAndSubCmd, provider) => {
      val (mode, subcmd) = modeAndSubCmd
      implicit val networkPrefix: NetworkType.NetworkPrefix = provider.networkPrefix
      modeAndSubCmd match {
        case (BramblCliMode.transaction, BramblCliSubCmd.create) =>
          validatTransactionCreate(paramConfig).mapN((_, token, fromAddresses, toAddresses, changeAddress, fee) =>
            BramblCliValidatedParams(
              mode,
              subcmd,
              provider,
              "",
              Some(token),
              paramConfig.someOutputFile,
              None,
              fromAddresses,
              toAddresses,
              changeAddress,
              fee
            )
          )
        case (BramblCliMode.wallet, BramblCliSubCmd.create) =>
          validateWalletCreate(paramConfig).map { password =>
            BramblCliValidatedParams(
              mode,
              subcmd,
              provider,
              password,
              None,
              paramConfig.someOutputFile,
              None,
              Nil,
              Nil,
              "",
              0
            )
          }
      }
    }).andThen(x => x)
  }

}
