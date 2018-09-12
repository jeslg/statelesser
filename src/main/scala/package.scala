package org.hablapps

import scalaz._, Scalaz._
import shapeless._

package object statelesser extends LensAlgHom.Syntax {

  type LensAlg[P[_], A] = LensAlgHom[MonadState, P, A]

  object LensAlg {
    def apply[P[_], A](hom: State[A, ?] ~> P): LensAlg[P, A] =
      LensAlgHom[MonadState, P, State[A, ?], A](implicitly, hom)
  }

  implicit def slensToLens[S, A](
      ln: shapeless.Lens[S, A]): naturally.Lens[S, A] =
    λ[State[A, ?] ~> State[S, ?]] { sa => 
      State(s => sa(ln.get(s)).leftMap(ln.set(s)))
    }

  def make[A](implicit ev: GetEvidence[HNil, A]): A = ev.apply()
}
