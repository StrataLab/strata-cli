package xyz.stratalab.strata.cli.modules

import co.topl.brambl.wallet.WalletApi

trait WalletApiModule extends WalletKeyApiModule {
  val walletApi = WalletApi.make(walletKeyApi)
}
