package xyz.stratalab.strata.cli.modules

import cats.effect.IO
import xyz.stratalab.strata.cli.impl.TxParserAlgebra

trait TxParserAlgebraModule extends TransactionBuilderApiModule {

  def txParserAlgebra(networkId: Int, ledgerId: Int) =
    TxParserAlgebra.make[IO](transactionBuilderApi(networkId, ledgerId))

}
