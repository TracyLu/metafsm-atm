package net.imadz;

import net.imadz.lifecycle.annotations.*;
import net.imadz.lifecycle.annotations.action.ConditionSet;
import net.imadz.lifecycle.annotations.action.Conditional;
import net.imadz.lifecycle.annotations.action.ConditionalEvent;
import net.imadz.lifecycle.annotations.state.Final;
import net.imadz.lifecycle.annotations.state.Initial;

@StateMachine
public interface ATMLifecycle {

    @StateSet
    interface States {
        @Initial
        @Transition(event = Events.Deposit.class, value = NonEmpty.class)
        class Empty {
        }

        @Transitions({@Transition(event = Events.Deposit.class, value = {NonEmpty.class, Full.class}),
                @Transition(event = Events.Withdraw.class, value = {NonEmpty.class, Empty.class}),
                @Transition(event = Events.Stop.class, value = Stopped.class)
        })
        class NonEmpty {
        }

        @Transitions({@Transition(event = Events.Withdraw.class, value = NonEmpty.class),
                @Transition(event = Events.Stop.class, value = Stopped.class)
        })
        class Full {
        }

        @Final
        class Stopped {}

    }

    @EventSet
    interface Events {

        @Conditional(condition = Conditions.CashCounter.class, judger = Utilities.TotalCashJudger.class, postEval = true)
        class Deposit {
        }

        @Conditional(condition = Conditions.CashCounter.class, judger = Utilities.TotalCashJudger.class, postEval = true)
        class Withdraw {
        }

        class Stop {}
    }

    @ConditionSet
     interface Conditions {

        interface CashCounter {

            Integer getTotalCash();
        }
    }

    class Utilities {

        public static class TotalCashJudger implements ConditionalEvent<Conditions.CashCounter> {
            @Override
            public Class<?> doConditionJudge(Conditions.CashCounter t) {
                if (t.getTotalCash() >= 1000) {
                    return States.Full.class;
                } else if (t.getTotalCash() < 1000 && t.getTotalCash() > 0) {
                    return States.NonEmpty.class;
                } else {
                    return States.Empty.class;
                }
            }
        }
    }
}