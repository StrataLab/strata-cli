package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import co.topl.brambl.servicekit.{WalletStateApi, WalletStateResource}

trait WalletStateAlgebraModule extends WalletStateResource with WalletApiModule with TransactionBuilderApiModule {

  def walletStateAlgebra(file: String) = WalletStateApi
    .make[IO](
      walletResource(file),
      walletApi
    )
}
