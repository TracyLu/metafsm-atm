package net.imadz

import net.imadz.ATMLifecycle.Events.{Recycle, Withdraw, Deposit}
import net.imadz.lifecycle.annotations.action.Condition
import net.imadz.lifecycle.annotations.{Event, StateIndicator, LifecycleMeta}

/**
 * Created by Scala on 15-7-8.
 */
@LifecycleMeta(classOf[ATMLifecycle])
case class ATM(operatorId: Int) extends ATMLifecycle.Conditions.CashCounter{
  private var totalCash: Int = 0

  @StateIndicator
  private var state = classOf[ATMLifecycle.States.Empty].getSimpleName

  @Condition(classOf[ATMLifecycle.Conditions.CashCounter])
  def getCashCounter() = this


  def getTotalCash(): Integer = totalCash

  def getState() = state

  @Event(classOf[Deposit])
  def deposit() = {
    totalCash += 100
  }


  @Event(classOf[Withdraw])
  def withdraw() = {
    totalCash -= 100
  }

  @Event(classOf[Recycle])
  def recycle() = {
    totalCash = 0
  }

}
