package statelesser

import scalaz._

trait OpticLang[Expr[_]] {

  def flVert[S, A, B](
    l: Expr[Fold[S, A]], 
    r: Expr[Fold[A, B]]): Expr[Fold[S, B]]

  def flHori[S, A, B](
    l: Expr[Fold[S, A]], 
    r: Expr[Fold[S, B]]): Expr[Fold[S, (A, B)]]

  def gtVert[S, A, B](
    l: Expr[Getter[S, A]], 
    r: Expr[Getter[A, B]]): Expr[Getter[S, B]]

  def gtHori[S, A, B](
    l: Expr[Getter[S, A]],
    r: Expr[Getter[S, B]]): Expr[Getter[S, (A, B)]]

  def gtSub[S](
    l: Expr[Getter[S, Int]],
    r: Expr[Getter[S, Int]]): Expr[Getter[S, Int]]

  def aflVert[S, A, B](
    l: Expr[AffineFold[S, A]], 
    r: Expr[AffineFold[A, B]]): Expr[AffineFold[S, B]]

  def aflHori[S, A, B](
    l: Expr[AffineFold[S, A]],
    r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, (A, B)]]

  def aflFirst[S, A, B](
    l: Expr[AffineFold[S, A]], 
    r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, A]]

  def filtered[S](p: Expr[Getter[S, Boolean]]): Expr[AffineFold[S, S]]

  def greaterThan: Expr[Getter[(Int, Int), Boolean]]

  def equal[A]: Expr[Getter[(A, A), Boolean]]

  def first[A, B]: Expr[Getter[(A, B), A]]

  def second[A, B]: Expr[Getter[(A, B), B]]

  def like[S, A](a: A): Expr[Getter[S, A]]

  def id[S]: Expr[Getter[S, S]]

  def not: Expr[Getter[Boolean, Boolean]]

  def exists[S, A](
    fl: Expr[Fold[S, A]], 
    p: Expr[Getter[A, Boolean]]): Expr[Getter[S, Boolean]]

  def getAll[S, A](fl: Expr[Fold[S, A]]): Expr[S => List[A]]

  def lnAsGt[S, A](ln: Expr[Lens[S, A]]): Expr[Getter[S, A]]

  def gtAsFl1[S, A](gt: Expr[Getter[S, A]]): Expr[Fold1[S, A]]

  def gtAsAfl[S, A](gt: Expr[Getter[S, A]]): Expr[AffineFold[S, A]]

  def aflAsFl[S, A](afl: Expr[AffineFold[S, A]]): Expr[Fold[S, A]]

  def fl1AsFl[S, A](fl1: Expr[Fold1[S, A]]): Expr[Fold[S, A]]

  // derived methods

  def gt(i: Int): Expr[Getter[Int, Boolean]] = 
    gtVert(gtHori(id, like(i)), greaterThan)

  def eq[A](a: A): Expr[Getter[A, Boolean]] =
    gtVert(gtHori(id[A], like[A, A](a)), equal[A])

  def contains[S, A](fl: Expr[Fold[S, A]], a: A): Expr[Getter[S, Boolean]] =
    exists(fl, eq(a))

  def all[S, A](fl: Expr[Fold[S, A]], p: Expr[Getter[A, Boolean]]) =
    gtVert(exists(fl, gtVert(p, not)), not)
}

object OpticLang {

  type TypeNme = String
  type OpticNme = String
  
  sealed abstract class OpticKind
  case object KLens extends OpticKind
  case object KGetter extends OpticKind
  case object KAffineFold extends OpticKind
  case object KFold1 extends OpticKind
  case object KFold extends OpticKind

  case class TypeInfo(nme: TypeNme, isPrimitive: Boolean = false)
  case class OpticInfo(
    kind: OpticKind, 
    nme: OpticNme, 
    src: TypeInfo, 
    tgt: TypeInfo)
  
  case class Semantic(
      table: Map[Var, Tree] = Map(),
      select: List[Tree] = List(),
      where: Set[Tree] = Set()) {
    
    def hCompose(other: Semantic): Semantic = {
      val Semantic(t1, s1, w1) = this
      val Semantic(t2, s2, w2) = other
      Semantic(t1 ++ t2, s1 ++ s2, w1 ++ w2)
    }

    def vCompose(other: Semantic): Semantic = {
      (this, other) match {
        case (Semantic(t1, List(v1: Var), s1), Semantic(_, Nil, s2)) 
            if s2.nonEmpty && s2.head.isInstanceOf[Sub] => {
          val Sub(sem) = s2.head
          Semantic(t1, List(v1), s1 + Sub(sem))
        }
        case (Semantic(t1, List(tr1), w1), Semantic(t2, s2, w2)) => Semantic(
          t1 ++ t2.mapValues(tr1.vCompose), 
          if (s2.isEmpty) List(tr1) else s2.map(tr1.vCompose), 
          w1 ++ w2.map(tr1.vCompose))
        case (Semantic(t1, List(tr1, tr2), w1), 
              Semantic(t2, List(Unapplied(f)), w2)) => {
          val tr3 = f(tr1)(tr2)
          Semantic(
            t1 ++ t2.mapValues(tr3.vCompose), 
            List(tr3),
            w1 ++ w2.map(tr3.vCompose))
        }
        case (Semantic(t1, List(tr1, tr2), w1), 
              Semantic(t2, List(Unapplied(f), Unapplied(g)), w2)) => {
          Semantic(t1, List(f(tr1)(tr2), g(tr1)(tr2)), w1 ++ w2)
        }
      }
    }

    def fstCompose(other: Semantic): Semantic = {
      val Semantic(t1, s1, w1) = this
      val Semantic(t2, s2, w2) = other
      Semantic(t1 ++ t2, s1, w1 ++ w2)
    }

    def subCompose(other: Semantic): Semantic = {
      val Semantic(t1, List(tr1), w1) = this
      val Semantic(t2, List(tr2), w2) = other
      Semantic(t1 ++ t2, List(Op("-", tr1, tr2)), w1 ++ w2)
    }
  }

  sealed abstract class Tree {
    def vCompose(other: Tree): Tree = (this, other) match {
      case (v: Var, l: GLabel) => VL(v, l)
      case (_, v: Var) => v
      case (_, vl: VL) => vl
      case (x, Op(op, l, r)) => Op(op, x vCompose l, x vCompose r)
      case (l, v: Val) => v
      case (l, u: Unapplied1) => u.f(l)
      case (_, s: Sub) => s
      case (_, u@Unary(Sub(_), _)) => u
      case (l, r) => throw new Error(s"Can't compose trees '$l' and '$r'")
    }
  }
  case class GLabel(info: OpticInfo) extends Tree
  case class Var(nme: String) extends Tree
  case class VL(vr: Var, lbl: GLabel) extends Tree
  case class Op(op: String, l: Tree, r: Tree) extends Tree
  case class Unary(t: Tree, op: String) extends Tree
  case class Unapplied(f: Tree => Tree => Tree) extends Tree
  case class Unapplied1(f: Tree => Tree) extends Tree
  case class Val(v: String) extends Tree
  case class Sub(s: Semantic) extends Tree

  implicit val semantic = new OpticLang[Const[Semantic, ?]] {

    def flVert[S, A, B](
        l: Const[Semantic, Fold[S, A]], 
        r: Const[Semantic, Fold[A, B]]) =
      Const(l.getConst vCompose r.getConst)

    def flHori[S, A ,B](
        l: Const[Semantic, Fold[S, A]],
        r: Const[Semantic, Fold[S, B]]) =
      Const(l.getConst hCompose r.getConst)

    def gtVert[S, A, B](
        l: Const[Semantic, Getter[S, A]], 
        r: Const[Semantic, Getter[A, B]]) =
      Const(l.getConst vCompose r.getConst)

    def gtHori[S, A, B](
        l: Const[Semantic, Getter[S, A]],
        r: Const[Semantic, Getter[S, B]]) =
      Const(l.getConst hCompose r.getConst)

    def gtSub[S](
        l: Const[Semantic, Getter[S, Int]],
        r: Const[Semantic, Getter[S, Int]]) =
      Const(l.getConst subCompose r.getConst)

    def filtered[S](p: Const[Semantic, Getter[S, Boolean]]) =
      Const(Semantic(Map(), List.empty, p.getConst.select.toSet))

    def greaterThan: Const[Semantic, Getter[(Int, Int), Boolean]] =
      Const(Semantic(Map(), List(Unapplied(l => r => Op(">", l, r)))))

    def equal[A] =
      Const(Semantic(Map(), List(Unapplied(l => r => Op("=", l, r)))))

    def aflAsFl[S, A](afl: Const[Semantic, AffineFold[S, A]]) =
      Const(afl.getConst)

    def aflHori[S, A, B](
        l: Const[Semantic, AffineFold[S, A]], 
        r: Const[Semantic, AffineFold[S, B]]) =
      Const(l.getConst hCompose r.getConst)

    def aflVert[S, A, B](
        l: Const[Semantic, AffineFold[S, A]], 
        r: Const[Semantic, AffineFold[A, B]]) =
      Const(l.getConst vCompose r.getConst)

    def aflFirst[S, A, B](
        l: Const[Semantic, AffineFold[S, A]], 
        r: Const[Semantic, AffineFold[S, B]]) =
      Const(l.getConst fstCompose r.getConst)

    def first[A, B] = Const(Semantic(select = List(Unapplied(l => _ => l))))

    def second[A, B] = Const(Semantic(select = List(Unapplied(_ => r => r))))

    def like[S, A](a: A) = Const(Semantic(select = List(Val(a.toString))))

    def id[S] = Const(Semantic(select = List(Unapplied1(identity))))

    def not = Const(Semantic(select = List(Unapplied1(t => Unary(t, "not")))))

    def exists[S, A](
        fl: Const[Semantic, Fold[S, A]], 
        p: Const[Semantic, Getter[A, Boolean]]): Const[Semantic, Getter[S, Boolean]] =
      Const(Semantic(select = List(Sub(fl.getConst vCompose filtered(p).getConst))))

    def gtAsAfl[S, A](gt: Const[Semantic, Getter[S, A]]) = Const(gt.getConst)

    def getAll[S, A](fl: Const[Semantic, Fold[S, A]]) = Const(fl.getConst)

    def lnAsGt[S, A](ln: Const[Semantic, Lens[S, A]]) = Const(ln.getConst)

    def gtAsFl1[S, A](gt: Const[Semantic, Getter[S, A]]) = Const(gt.getConst)

    def fl1AsFl[S, A](fl1: Const[Semantic, Fold1[S, A]]) = Const(fl1.getConst)
  }

  trait Syntax {

    implicit class FoldOps[Expr[_], S, A](
        l: Expr[Fold[S, A]])(implicit 
        O: OpticLang[Expr]) {
      def >[B](r: Expr[Fold[A, B]]): Expr[Fold[S, B]] = O.flVert(l, r)
      def *[B](r: Expr[Fold[S, B]]): Expr[Fold[S, (A, B)]] = O.flHori(l, r)
    }

    implicit class Fold1Ops[Expr[_], S, A](
        l: Expr[Fold1[S, A]])(implicit
        O: OpticLang[Expr]) {
      def asFold: Expr[Fold[S, A]] = O.fl1AsFl(l)
    }

    implicit class AffineFoldOps[Expr[_], S, A](
        l: Expr[AffineFold[S, A]])(implicit
        O: OpticLang[Expr]) {
      def asFold: Expr[Fold[S, A]] = O.aflAsFl(l)
      def >[B](r: Expr[AffineFold[A, B]]): Expr[AffineFold[S, B]] = 
        O.aflVert(l, r)
      def *[B](r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, (A, B)]] = 
        O.aflHori(l, r)
      def <*[B](r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, A]] =
        O.aflFirst(l, r)
    }

    implicit class GetterOps[Expr[_], S, A](
        l: Expr[Getter[S, A]])(implicit
        O: OpticLang[Expr]) {
      def asFold1: Expr[Fold1[S, A]] = O.gtAsFl1(l)
      def asAffineFold: Expr[AffineFold[S, A]] = O.gtAsAfl(l)
      def asFold: Expr[Fold[S, A]] = O.fl1AsFl(O.gtAsFl1(l))
      def >[B](r: Expr[Getter[A, B]]): Expr[Getter[S, B]] = O.gtVert(l, r)
      def *[B](r: Expr[Getter[S, B]]): Expr[Getter[S, (A, B)]] = O.gtHori(l, r)
    }

    implicit class GetterIntOps[Expr[_], S](
        l: Expr[Getter[S, Int]])(implicit
        O: OpticLang[Expr]) {
      def -(r: Expr[Getter[S, Int]]): Expr[Getter[S, Int]] = O.gtSub(l, r)
    }
  }

  object syntax extends Syntax
}
