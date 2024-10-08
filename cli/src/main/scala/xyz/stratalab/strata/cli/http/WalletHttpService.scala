package xyz.stratalab.strata.cli.http

import cats.Id
import cats.effect.{IO, _}
import co.topl.brambl.dataApi.{GenusQueryAlgebra, WalletStateAlgebra}
import co.topl.brambl.servicekit.{FellowshipStorageApi, TemplateStorageApi}
import co.topl.brambl.utils.Encoding
import io.circe.generic.auto._
import io.circe.syntax._
import io.grpc.ManagedChannel
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, _}
import xyz.stratalab.shared.models.{AssetTokenBalanceDTO, BalanceRequestDTO, BalanceResponseDTO, FellowshipDTO, GroupTokenBalanceDTO, LvlBalance, NetworkResponseDTO, SeriesTokenBalanceDTO, SimpleErrorDTO, TemplateDTO}
import xyz.stratalab.strata.cli.impl.WalletModeHelper

import java.sql.Connection

case class WalletHttpService(
  walletStateAlgebra: WalletStateAlgebra[IO],
  channelResource:    Resource[IO, ManagedChannel],
  walletResource:     Resource[IO, Connection]
) {

  def walletService(networkName: String, networkId: String) =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "balance" =>
        implicit val balanceReqdecoder: EntityDecoder[IO, BalanceRequestDTO] =
          jsonOf[IO, BalanceRequestDTO]
        (for {
          input <- req.as[BalanceRequestDTO]
          balanceEither <- WalletModeHelper[IO](
            walletStateAlgebra,
            GenusQueryAlgebra
              .make[IO](
                channelResource
              )
          ).getBalance(
            None,
            Some(input.fellowship),
            Some(input.template),
            input.interaction.map(_.toInt)
          ).map(
            _.left
              .map { e =>
                new IllegalArgumentException(e)
              }
          )
          balances <- IO.fromEither(balanceEither)
          res <- Ok(
            balances
              .foldLeft(
                BalanceResponseDTO(
                  "0",
                  List.empty,
                  List.empty,
                  List.empty
                )
              ) { (acc, x) =>
                (x: @unchecked) match { // we have filtered out Unknown tokens
                  case LvlBalance(b) => acc.copy(lvlBalance = b)
                  case GroupTokenBalanceDTO(g, b) =>
                    acc.copy(groupBalances = acc.groupBalances :+ GroupTokenBalanceDTO(g, b))
                  case SeriesTokenBalanceDTO(id, balance) =>
                    acc.copy(seriesBalances = acc.seriesBalances :+ SeriesTokenBalanceDTO(id, balance))
                  case AssetTokenBalanceDTO(group, series, balance) =>
                    acc.copy(assetBalances =
                      acc.assetBalances :+ AssetTokenBalanceDTO(
                        group,
                        series,
                        balance
                      )
                    )
                }
              }
              .asJson
          )
        } yield res).handleErrorWith { t =>
          t.printStackTrace()
          InternalServerError(
            SimpleErrorDTO(t.getMessage()).asJson,
            headers.`Content-Type`(MediaType.text.html)
          )
        }
      case GET -> Root / "network" =>
        Ok(
          NetworkResponseDTO(
            networkName,
            Encoding.encodeToHex(BigInt(networkId).toByteArray)
          ).asJson
        )
      case GET -> Root / "fellowships" =>
        val fellowshipStorageAlgebra = FellowshipStorageApi.make[IO](
          walletResource
        )
        fellowshipStorageAlgebra.findFellowships().flatMap { fellowships =>
          Ok(fellowships.map(x => FellowshipDTO(x.xIdx, x.name)).asJson)
        }
      case GET -> Root / "templates" =>
        val templateStorageAlgebra = TemplateStorageApi.make[IO](
          walletResource
        )
        import xyz.stratalab.strata.cli.views.WalletModelDisplayOps._
        import io.circe.parser.parse
        import co.topl.brambl.codecs.LockTemplateCodecs._
        import cats.implicits._
        for {
          templates <- templateStorageAlgebra.findTemplates()
          resTemplates <- templates.traverse { x =>
            IO(
              (for {
                json    <- parse(x.lockTemplate)
                decoded <- decodeLockTemplate[Id](json)
              } yield TemplateDTO(
                x.yIdx,
                x.name,
                serialize[Id](decoded)
              )).toOption.get
            )
          }
          res <- Ok(resTemplates.asJson)
        } yield res

    }
}
