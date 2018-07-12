package org.hablapps.statelesser

package test
package university

import scalaz._
import org.scalatest._
import Util._, GetEvidence._, Primitive._


/* Data Model */

case class City[P[_], C, U, D](
  self: Field[P, C],
  name: StringP[P],
  popu: IntegerP[P],
  univ: University[P, U, D])

case class University[P[_], U, D](
  self: Field[P, U],
  name: StringP[P],
  math: Department[P, D])

case class Department[P[_],D](
  self: Field[P,D],
  budget: IntegerP[P])


/* Logic */

case class Logic[P[_], C, U, D](city: City[P, C, U, D]) {

  def getPopu: P[Int] =
    city.popu.get()

  def doubleBudget(implicit M: Monad[P]): P[Unit] =
    city.univ.math.budget.modify(_ * 2)

  // should be reused
  def getMathDep: P[D] =
    city.univ.math.self.get()

  def uppercaseUnivName(implicit M: Monad[P]): P[Unit] =
    city.univ.name.modify(_.toUpperCase)
}


/* Interpretation */

case class SCity(popu: Int, name: String, univ: SUniversity)
case class SUniversity(name: String, math: SDepartment)
case class SDepartment(budget: Int)

class UniversitySpec extends FlatSpec with Matchers {

  "Automagic instances" should "be generated for city" in {

    val StateCity = make[City[State[SCity, ?], SCity, SUniversity, SDepartment]]

    val logic = Logic(StateCity)

    val urjc = SUniversity("urjc", SDepartment(3000))
    val most = SCity(200000, "mostoles", urjc)

    val popu2 = logic.getPopu.eval(most)
    val most2 = logic.doubleBudget(implicitly).exec(most)
    val math2 = logic.getMathDep.eval(most2)

    popu2 should be (200000)
    most2 should be (most.copy(univ = SUniversity("urjc", SDepartment(6000))))
    math2 should be (SDepartment(6000))

    val most3 = logic.uppercaseUnivName.exec(most)

    most3 should be (most.copy(univ = SUniversity("URJC", SDepartment(3000))))
  }

  it should "be generated for university" in {
    make[University[State[SUniversity, ?], SUniversity, SDepartment]]
  }

  it should "be generated for department" in {
    make[Department[State[SDepartment, ?], SDepartment]]
  }
}

