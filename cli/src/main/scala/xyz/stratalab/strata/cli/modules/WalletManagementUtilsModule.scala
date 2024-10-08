package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.impl.WalletManagementUtils

trait WalletManagementUtilsModule extends WalletApiModule {

  val walletManagementUtils =
    new WalletManagementUtils[IO](walletApi, walletKeyApi)
}
