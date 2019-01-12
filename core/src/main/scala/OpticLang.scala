package statelesser

import scalaz._, Scalaz._
import Leibniz._

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

  def aflVert[S, A, B](
    l: Expr[AffineFold[S, A]],
    r: Expr[AffineFold[A, B]]): Expr[AffineFold[S, B]]

  def aflHori[S, A, B](
    l: Expr[AffineFold[S, A]],
    r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, (A, B)]]

  def filtered[S](p: Expr[Getter[S, Boolean]]): Expr[AffineFold[S, S]]

  def sub: Expr[Getter[(Int, Int), Int]]

  def greaterThan: Expr[Getter[(Int, Int), Boolean]]

  def equal[A]: Expr[Getter[(A, A), Boolean]]

  def first[A, B]: Expr[Getter[(A, B), A]]

  def second[A, B]: Expr[Getter[(A, B), B]]

  def likeInt[S](i: Int): Expr[Getter[S, Int]]

  def likeBool[S](b: Boolean): Expr[Getter[S, Boolean]]

  def likeStr[S](s: String): Expr[Getter[S, String]]

  def id[S]: Expr[Getter[S, S]]

  def not: Expr[Getter[Boolean, Boolean]]

  def getAll[S, A](fl: Expr[Fold[S, A]]): Expr[S => List[A]]

  def lnAsGt[S, A](ln: Expr[Lens[S, A]]): Expr[Getter[S, A]]

  def gtAsAfl[S, A](gt: Expr[Getter[S, A]]): Expr[AffineFold[S, A]]

  def aflAsFl[S, A](afl: Expr[AffineFold[S, A]]): Expr[Fold[S, A]]

  // derived methods

  def gt(i: Int): Expr[Getter[Int, Boolean]] =
    gtVert(gtHori(id, likeInt(i)), greaterThan)

  def gtAsFl[S, A](gt: Expr[Getter[S, A]]): Expr[Fold[S, A]] =
    aflAsFl(gtAsAfl(gt))

  // def eq(i: Int): Expr[Getter[Int, Boolean]] =
  //   gtVert(gtHori(id, like(i)), equal[A])

  // def contains[S, A](fl: Expr[Fold[S, A]], a: A): Expr[Getter[S, Boolean]] =
  //   exists(fl, eq(a))

  // def all[S, A](fl: Expr[Fold[S, A]], p: Expr[Getter[A, Boolean]]) =
  //   gtVert(exists(fl, gtVert(p, not)), not)
}

object OpticLang {

  def apply[E[_]](implicit ev: OpticLang[E]): OpticLang[E] = ev

  implicit val prettyPrinter = new OpticLang[Const[String, ?]] {

    def flVert[S, A, B](
        l: Const[String, Fold[S, A]],
        r: Const[String, Fold[A, B]]): Const[String, Fold[S, B]] =
      Const(s"${l.getConst} > ${r.getConst}")

    def flHori[S, A, B](
        l: Const[String, Fold[S, A]],
        r: Const[String, Fold[S, B]]): Const[String, Fold[S, (A, B)]] =
      Const(s"${l.getConst} * ${r.getConst}")

    def gtVert[S, A, B](
        l: Const[String, Getter[S, A]],
        r: Const[String, Getter[A, B]]): Const[String, Getter[S, B]] =
      Const(s"${l.getConst} > ${r.getConst}")

    def gtHori[S, A, B](
        l: Const[String, Getter[S, A]],
        r: Const[String, Getter[S, B]]): Const[String, Getter[S, (A, B)]] =
      Const(s"(${l.getConst} * ${r.getConst})")

    def aflVert[S, A, B](
        l: Const[String, AffineFold[S, A]],
        r: Const[String, AffineFold[A, B]]): Const[String, AffineFold[S, B]] =
      Const(s"${l.getConst} > ${r.getConst}")

    def aflHori[S, A, B](
        l: Const[String, AffineFold[S, A]],
        r: Const[String, AffineFold[S, B]]): Const[String, AffineFold[S, (A, B)]] =
      Const(s"${l.getConst} * ${r.getConst}")

    def filtered[S](
        p: Const[String, Getter[S, Boolean]]): Const[String, AffineFold[S, S]] =
      Const(s"filtered(${p.getConst})")

    def sub: Const[String, Getter[(Int, Int), Int]] =
      Const("sub")

    def greaterThan: Const[String, Getter[(Int, Int), Boolean]] =
      Const("greaterThan")

    def equal[A]: Const[String, Getter[(A, A), Boolean]] =
      Const("equal")

    def first[A, B]: Const[String, Getter[(A, B), A]] =
      Const("first")

    def second[A, B]: Const[String, Getter[(A, B), B]] =
      Const("second")

    def likeInt[S](i: Int): Const[String, Getter[S, Int]] =
      Const(s"likeInt(${i.toString})")

    def likeBool[S](b: Boolean): Const[String, Getter[S, Boolean]] =
      Const(s"likeBool(${b.toString})")

    def likeStr[S](s: String): Const[String, Getter[S, String]] =
      Const(s"""likeStr("$s")""")

    def id[S]: Const[String, Getter[S, S]] =
      Const("id")

    def not: Const[String, Getter[Boolean, Boolean]] =
      Const("not")

    def getAll[S, A](
        fl: Const[String, Fold[S, A]]): Const[String, S => List[A]] =
      Const(s"getAll(${fl.getConst})")

    def lnAsGt[S, A](
        ln: Const[String, Lens[S, A]]): Const[String, Getter[S, A]] =
      Const(s"${ln.getConst}.asGetter")

    def gtAsFl1[S, A](
        gt: Const[String, Getter[S, A]]): Const[String, Fold1[S, A]] =
      Const(s"${gt.getConst}.asFold1")

    def gtAsAfl[S, A](
        gt: Const[String, Getter[S, A]]): Const[String, AffineFold[S, A]] =
      Const(s"${gt.getConst}.asAffineFold")

    def aflAsFl[S, A](
        afl: Const[String, AffineFold[S, A]]): Const[String, Fold[S, A]] =
      Const(s"${afl.getConst}.asFold")

    def fl1AsFl[S, A](
        fl1: Const[String, Fold1[S, A]]): Const[String, Fold[S, A]] =
      Const(s"${fl1.getConst}.asFold")
  }

  sealed abstract class OpticKind
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

  trait OpticMap[E[_], O[_, _], O2[_, _]] {
    def apply[S, A](e: E[O[S, A]]): E[O2[S, A]]
  }

  case class Table(rows: Map[Symbol, Any] = Map()) {

    def consistency: (Table, Set[(String, String)]) = {
      val (t, rws) = rows
        .groupBy(_._2)
        .map { case (k, v) => (k, v.keys.toList) }
        .foldLeft((Table(), Set.empty[(String, String)])) {
          case ((Table(rs), rws), (v, k :: ks)) =>
            (Table(rs + (k -> v)), rws ++ ks.map((_, k)))
        }
      if (rws.isEmpty) (t, rws)
      else t.rwVars(rws).consistency.map(rws ++ _)
    }

    def unify(other: Table): (Table, Set[(String, String)]) =
      Table(rows ++ other.rows).consistency

    def clean(used: Set[String]): Table = {
      def deps(k: String): Set[String] = rows(k) match {
        case TVarNestedVal(x: Var[Any, Any, Any, Any], w) =>
          deps(x.name) + x.name
        case _ => Set()
      }
      Table(rows.filterKeys((used ++ (used flatMap deps)).contains(_)))
    }

    def rwVars(rws: Set[(String, String)]): Table =
      Table(rows.mapValues(_.asInstanceOf[TVarVal[Any, Any, _, _]].rwVars(rws)))
  }

  implicit class TableOps(table: Table) {

    private def splitTables = table.rows.partition {
      case (_, TVarSimpleVal(_)) => true
      case _ => false
    }

    def getVal[E[_], O[_, _], S, A](v: Var[E, O, S, A]): TVarVal[E, O, S, A] =
      table.rows(v.name).asInstanceOf[TVarVal[E, O, S, A]]

    def simpleTable[E[_], O[_, _]]: Map[String, TVarSimpleVal[E, O, _, _]] =
      splitTables._1.asInstanceOf[Map[String, TVarSimpleVal[E, O, _, _]]]

    def nestedTable[E[_], O[_, _]]: Map[String, TVarNestedVal[E, O, _, _, _]] =
      splitTables._2.asInstanceOf[Map[String, TVarNestedVal[E, O, _, _, _]]]
  }

  val varStr: Stream[String] = {
    def syms(pattern: Stream[String], i: Int = 0): Stream[String] =
      pattern.map(_ + i) #::: syms(pattern, i + 1)
    val pattern = Stream.range('a', 'z').map(_.toString)
    pattern #::: syms(pattern)
  }

  def reifySem[E[_], S, A](
      sem: Semantic[E, Fold[S, A]])(implicit
      ev: OpticLang[E]): E[Fold[S, A]] = {
    val TFold(expr, filt, t) = sem.eval(varStr)
    filt.map(reifyExpr(_, t)).foldLeft(reifyExpr(expr, t)) { (acc, e) =>
      ev.flVert(ev.flHori(acc, e), ev.aflAsFl(ev.gtAsAfl(ev.first[A, Boolean])))
    }
  }

  def reifyVV[E[_], S, A](
      vv: TVarVal[E, Fold, S, A],
      t: Table)(implicit
      ev: OpticLang[E]): E[Fold[S, A]] = vv match {
    case TVarSimpleVal(w) => w.e
    case TVarNestedVal(v, w) =>
      ev.flVert(reifyVV(t.getVal(v), t), reifyExpr(w, t))
  }

  def reifyExpr[E[_], S, A](
      expr: TExpr[E, Fold, S, A],
      t: Table)(implicit
      ev: OpticLang[E]): E[Fold[S, A]] = expr match {
    case Product(l, r, is) =>
      is.subst[λ[x => E[Fold[S, x]]]](ev.flHori(reifyExpr(l, t), reifyExpr(r, t)))
    case Vertical(u, d) => ev.flVert(reifyExpr(u, t), reifyExpr(d, t))
    case x: Var[E, Fold, S, A] => reifyVV(t.getVal(x), t)
    case w: Wrap[E, Fold, S, A] => w.e
    case LikeInt(i, is) =>
      is.flip.subst[λ[x => E[Fold[S, x]]]](ev.gtAsFl(ev.likeInt[S](i)))
    case LikeBool(b, is) =>
      is.flip.subst[λ[x => E[Fold[S, x]]]](ev.gtAsFl(ev.likeBool[S](b)))
    case Id(is) => is.subst[λ[x => E[Fold[S, x]]]](ev.gtAsFl(ev.id[S]))
    case First(is) => is.flip.subst[λ[x => E[Fold[x, A]]]](ev.gtAsFl(ev.first))
    case Second(is) => is.flip.subst[λ[x => E[Fold[x, A]]]](ev.gtAsFl(ev.second))
    case Not(is1, is2) =>
      is2.flip.subst[λ[x => E[Fold[S, x]]]](
        is1.flip.subst[λ[x => E[Fold[x, Boolean]]]](ev.gtAsFl(ev.not)))
    case Sub(is1, is2) =>
      is2.flip.subst[λ[x => E[Fold[S, x]]]](
        is1.flip.subst[λ[x => E[Fold[x, Int]]]](ev.gtAsFl(ev.sub)))
    case Gt(is1, is2) =>
      is2.flip.subst[λ[x => E[Fold[S, x]]]](
        is1.flip.subst[λ[x => E[Fold[x, Boolean]]]](ev.gtAsFl(ev.greaterThan)))
  }

  import State._

  implicit def tsemantic[E[_]: OpticLang]: OpticLang[Semantic[E, ?]] =
      new OpticLang[Semantic[E, ?]] {

    private def vertAux[O[_, _], S, A, B](
        u: TExpr[E, O, S, A],
        d: TExpr[E, O, A, B]): WrapTable[TExpr[E, O, S, B]] =
      (u, d) match {
        case (_, Product(l, r, is)) =>
          (vertAux(u, l) |@| vertAux(u, r)) { (l1, r1) =>
            TExpr.product(l1, r1, is)
          }
        case (_, Vertical(u1, d1)) =>
          vertAux(u, u1) >>= (u2 => vertAux(u2, d1))
        case (Product(l /* : TExpr[E, O, S, B] */, _, _), First(is)) =>
          l.asInstanceOf[TExpr[E, O, S, B]].point[WrapTable]
        case (Product(_, r /* : TExpr[E, O, S, B] */, _), Second(is)) =>
          r.asInstanceOf[TExpr[E, O, S, B]].point[WrapTable]
        case (Id(is), e) =>
          is.flip.subst[λ[x => TExpr[E, O, x, B]]](e).point[WrapTable]
        case (e, Id(is)) =>
          is.subst[λ[x => TExpr[E, O, S, x]]](e).point[WrapTable]
        case (_, LikeInt(i, is)) =>
          TExpr.likeInt(i, is).point[WrapTable]
        case (_, LikeBool(b, is)) =>
          TExpr.likeBool(b, is).point[WrapTable]
        case (Product(l /*: TExpr[E, O, S, Int] */, LikeInt(0, _), _), Sub(_, is)) =>
          is.flip.subst[λ[x => TExpr[E, O, S, x]]](
            l.asInstanceOf[TExpr[E, O, S, Int]]).point[WrapTable]
        case (Product(LikeInt(x, _), LikeInt(y, _), _), Sub(_, is)) =>
          TExpr.likeInt(x - y, is).point[WrapTable]
        case (Product(LikeInt(x, _), LikeInt(y, _), _), Gt(_, is)) =>
          TExpr.likeBool(x > y, is).point[WrapTable]
        case (LikeBool(b, _), Not(_, is)) =>
          TExpr.likeBool(! b, is).point[WrapTable]
        case (Vertical(e, Not(is1, _)), Not(_, is2)) =>
          is2.flip.subst(is1.subst(e)).point[WrapTable]
        case (Not(is1, _), Not(_, is2)) =>
          TExpr.id[E, O, S, B](is2.flip compose is1).point[WrapTable]
        case (x@Var(_), w@Wrap(_, inf)) if inf.tgt.isPrimitive =>
          assignVal(TVarNestedVal(x, w))
        case _ => TExpr.vertical(u, d).point[WrapTable]
      }

    def gtVert[S, A, B](
        usem: Semantic[E, Getter[S, A]],
        dsem: Semantic[E, Getter[A, B]]): Semantic[E, Getter[S, B]] =
      for {
        tmp <- usem
        TGetter(expr1) = tmp
        tmp <- dsem
        TGetter(expr2) = tmp
        tmp <- vertAux(expr1, expr2).run(Table())
        (_, expr3) = tmp
      } yield TGetter(expr3)

    def gtHori[S, A, B](
        l: Semantic[E, Getter[S, A]],
        r: Semantic[E, Getter[S, B]]): Semantic[E, Getter[S, (A, B)]] =
      for {
        tmp <- l
        TGetter(expr1) = tmp
        tmp <- r
        TGetter(expr2) = tmp
      } yield TGetter(Product(expr1, expr2, Leibniz.refl[(A, B)]))

    def aflVert[S, A, B](
        u: Semantic[E, AffineFold[S, A]],
        d: Semantic[E, AffineFold[A, B]]): Semantic[E, AffineFold[S, B]] =
      for {
        tmp <- u
        TAffineFold(expr1, filt1) = tmp
        tmp <- d
        TAffineFold(expr2, filt2) = tmp
        tmp <- (for {
          expr <- vertAux(expr1, expr2)
          filt <- filt2.toList.traverse(vertAux(expr1, _))
        } yield (expr, filt)).run(Table())
        (_, (expr3, filt3)) = tmp
      } yield TAffineFold(expr3, filt1 ++ filt3)

    def aflHori[S, A, B](
        l: Semantic[E, AffineFold[S, A]],
        r: Semantic[E, AffineFold[S, B]]) =
      for {
        tmp <- l
        TAffineFold(expr1, filt1) = tmp
        tmp <- r
        TAffineFold(expr2, filt2) = tmp
      } yield TAffineFold(
          Product(expr1, expr2, Leibniz.refl[(A, B)]),
          filt1 ++ filt2)

    def flVert[S, A, B](
        u: Semantic[E, Fold[S, A]],
        d: Semantic[E, Fold[A, B]]) =
      for {
        tmp <- u
        TFold(expr1, filt1, vars1) = tmp
        tmp <- d
        TFold(expr2, filt2, _) = tmp
        tmp <- (for {
          expr <- vertAux(expr1, expr2)
          filt <- filt2.toList.traverse(vertAux(expr1, _))
        } yield (expr, filt)).run(vars1)
        (vars3, (expr3, filt3)) = tmp
      } yield TFold(expr3, filt1 ++ filt3, vars3.clean(
          expr3.vars ++
          filt1.foldLeft(Set.empty[String])(_ ++ _.vars) ++
          filt3.foldLeft(Set.empty[String])(_ ++ _.vars)))

    def flHori[S, A, B](
        l: Semantic[E, Fold[S, A]],
        r: Semantic[E, Fold[S, B]]): Semantic[E, Fold[S, (A, B)]] =
      for {
        tmp <- l
        TFold(expr1, filt1, vars1) = tmp
        tmp <- r
        TFold(expr2, filt2, vars2) = tmp
        (vars3, rws) = vars1.unify(vars2)
      } yield TFold(
          Product(expr1.rwVars(rws), expr2.rwVars(rws), Leibniz.refl[(A, B)]),
          filt1.map(_.rwVars(rws)) ++ filt2.map(_.rwVars(rws)),
          vars3)

    def filtered[S](
        p: Semantic[E, Getter[S, Boolean]]): Semantic[E, AffineFold[S, S]] =
      for {
        tmp <- gtAsAfl(p)
        TAffineFold(fil, _) = tmp
        tmp <- gtAsAfl(id[S])
        // XXX: it's still unclear what should we do with a predicate
        // containing filters itself,  so we just ignore it.
        TAffineFold(exp, _) = tmp
      } yield TAffineFold(expr = exp, filt = Set(fil))

    def sub: Semantic[E, Getter[(Int, Int), Int]] =
      state(TGetter(expr = Sub(implicitly, implicitly)))

    def greaterThan: Semantic[E, Getter[(Int, Int), Boolean]] =
      state(TGetter(expr = Gt(implicitly, implicitly)))

    def equal[A]: Semantic[E, Getter[(A, A), Boolean]] = ???

    def first[A, B]: Semantic[E, Getter[(A, B), A]] =
      state(TGetter(expr = First(Leibniz.refl[(A, B)])))

    def second[A, B]: Semantic[E, Getter[(A, B), B]] =
      state(TGetter(expr = Second(Leibniz.refl[(A, B)])))

    def likeInt[S](i: Int): Semantic[E, Getter[S, Int]] =
      state(TGetter(expr = LikeInt(i, implicitly)))

    def likeBool[S](b: Boolean): Semantic[E, Getter[S, Boolean]] =
      state(TGetter(expr = LikeBool(b, implicitly)))

    def likeStr[S](s: String): Semantic[E, Getter[S, String]] =
      ???

    def id[S]: Semantic[E, Getter[S, S]] =
      state(TGetter(expr = Id(Leibniz.refl)))

    def not: Semantic[E, Getter[Boolean, Boolean]] =
      state(TGetter(expr = Not(Leibniz.refl[Boolean], Leibniz.refl[Boolean])))

    def getAll[S, A](fl: Semantic[E, Fold[S, A]]): Semantic[E, S => List[A]] = ???

    def lnAsGt[S, A](ln: Semantic[E, Lens[S, A]]): Semantic[E, Getter[S, A]] = ???

    def gtAsAfl[S, A](
        gt: Semantic[E, Getter[S, A]]): Semantic[E, AffineFold[S, A]] =
      for {
        tmp <- gt
        TGetter(expr) = tmp
        f = new OpticMap[E, Getter, AffineFold] {
          def apply[X, Y](gt: E[Getter[X, Y]]) = OpticLang[E].gtAsAfl(gt)
        }
      } yield TAffineFold(expr.mapO(f))

    def aflAsFl[S, A](
        afl: Semantic[E, AffineFold[S, A]]): Semantic[E, Fold[S, A]] =
      for {
        tmp <- afl
        TAffineFold(expr, filt) = tmp
        f = new OpticMap[E, AffineFold, Fold] {
          def apply[X, Y](afl: E[AffineFold[X, Y]]) = OpticLang[E].aflAsFl(afl)
        }
      } yield TFold(expr.mapO(f), filt.map(_.mapO(f)), Table())
  }

  def fresh: State[Stream[String], String] =
    for {
      s <- get[Stream[String]]
      _ <- modify[Stream[String]](_.tail)
    } yield s.head

  type WrapTable[A] = StateT[State[Stream[String], ?], Table, A]

  def assignVal[E[_], O[_, _], S, A](
      vv: TVarVal[E, O, S, A]): WrapTable[TExpr[E, O, S, A]] =
    for {
      os <- MonadState[WrapTable, Table].gets(_.rows.find(_._2 == vv).map(_._1))
      s <- os.fold(
        StateT.StateMonadTrans[Table].liftM(fresh) >>! { s =>
          MonadState[WrapTable, Table].modify(t => Table(t.rows + (s -> vv)))
        })(_.point[WrapTable])
    } yield Var(s)

  trait Syntax {

    implicit class FoldOps[Expr[_], S, A](
        l: Expr[Fold[S, A]])(implicit
        O: OpticLang[Expr]) {
      def >[B](r: Expr[Fold[A, B]]): Expr[Fold[S, B]] = O.flVert(l, r)
      def *[B](r: Expr[Fold[S, B]]): Expr[Fold[S, (A, B)]] = O.flHori(l, r)
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
        O.aflVert(O.aflHori(l, r), O.gtAsAfl(O.first[A, B]))
      def *>[B](r: Expr[AffineFold[S, B]]): Expr[AffineFold[S, B]] =
        O.aflVert(O.aflHori(l, r), O.gtAsAfl(O.second[A, B]))
    }

    implicit class GetterOps[Expr[_], S, A](
        l: Expr[Getter[S, A]])(implicit
        O: OpticLang[Expr]) {
      def asAffineFold: Expr[AffineFold[S, A]] = O.gtAsAfl(l)
      def asFold: Expr[Fold[S, A]] = O.aflAsFl(O.gtAsAfl(l))
      def >[B](r: Expr[Getter[A, B]]): Expr[Getter[S, B]] = O.gtVert(l, r)
      def *[B](r: Expr[Getter[S, B]]): Expr[Getter[S, (A, B)]] = O.gtHori(l, r)
    }

    implicit class IntGetterOps[Expr[_], S](
        l: Expr[Getter[S, Int]])(implicit
        O: OpticLang[Expr]) {
      def -(r: Expr[Getter[S, Int]]): Expr[Getter[S, Int]] =
        O.gtVert(O.gtHori(l, r), O.sub)
    }
  }

  object syntax extends Syntax
}

