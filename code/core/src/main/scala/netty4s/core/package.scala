package netty4s

import cats.effect.Bracket

package object core {
  type BracketT[F[_]] = Bracket[F, Throwable]
}
