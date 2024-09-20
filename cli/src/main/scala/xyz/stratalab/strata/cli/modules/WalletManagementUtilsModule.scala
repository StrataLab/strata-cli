package xyz.stratalab.strata.cli.modules

import xyz.stratalab.strata.cli.impl.WalletManagementUtils
import cats.effect.IO

trait WalletManagementUtilsModule extends WalletApiModule {

  val walletManagementUtils =
    new WalletManagementUtils[IO](walletApi, walletKeyApi)
}
