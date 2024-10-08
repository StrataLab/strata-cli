package xyz.stratalab.strata.cli.controllers

import cats.Monad
import cats.effect.IO
import xyz.stratalab.strata.cli.mockbase.BaseWalletStateAlgebra
import xyz.stratalab.strata.cli.modules.DummyObjects
import co.topl.brambl.dataApi.GenusQueryAlgebra
import co.topl.brambl.display.DisplayOps.DisplayTOps
import co.topl.brambl.models.LockAddress
import co.topl.genus.services.{Txo, TxoState}
import munit.CatsEffectSuite

class GenusQueryControllerSpec extends CatsEffectSuite with DummyObjects {

  def makeWalletStateAlgebraMock[F[_]: Monad] = new BaseWalletStateAlgebra[F] {

    override def getAddress(
        fellowship: String,
        template: String,
        interaction: Option[Int]
    ): F[Option[String]] = Monad[F].pure(None)
  }
  def makeWalletStateAlgebraMockWithAddress[F[_]: Monad] =
    new BaseWalletStateAlgebra[F] {

      override def getAddress(
          fellowship: String,
          template: String,
          interaction: Option[Int]
      ): F[Option[String]] = Monad[F].pure(
        Some("ptetP7jshHVrEKqDRdKAZtuybPZoMWTKKM2ngaJ7L5iZnxP5BprDB3hGJEFr")
      )
    }

  def makeGenusQueryAlgebraMock[F[_]: Monad] = new GenusQueryAlgebra[F] {

    override def queryUtxo(
        fromAddress: LockAddress,
        txoState: TxoState
    ): F[Seq[Txo]] = Monad[F].pure(Seq.empty)

  }

  test(
    "queryUtxoFromParams should return an error if the address is not there"
  ) {
    val walletStateAlgebra = makeWalletStateAlgebraMock[IO]
    val genusQueryAlgebra = makeGenusQueryAlgebraMock[IO]
    val genusQueryController =
      new GenusQueryController[IO](walletStateAlgebra, genusQueryAlgebra)
    val result =
      genusQueryController.queryUtxoFromParams(None, "fellowship", "template", None)
    assertIO(result, Left("Address not found"))
  }

  test(
    "queryUtxoFromParams should return a formatted string if the address is there"
  ) {
    val walletStateAlgebra = makeWalletStateAlgebraMockWithAddress[IO]
    val genusQueryAlgebra = makeGenusQueryAlgebraMockWithOneAddress[IO]
    val genusQueryController =
      new GenusQueryController[IO](walletStateAlgebra, genusQueryAlgebra)
    val result =
      genusQueryController.queryUtxoFromParams(None, "fellowship", "template", None)
    assertIO(
      result,
      Right(txo01.display)
    )
  }
}
