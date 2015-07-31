package net.imadz;

/**
 * Created by Tracy on 7/31/15.
 */
public class ATM {
    public static final String EMPTY = "Empty";
    public static final String NON_EMPTY = "NonEmpty";
    public static final String FULL = "Full";
    public static final String RECYCLED = "Recycled";
    private int totalCash = 0;
    private String state = EMPTY;
    private int userId;

    public ATM(int userId) {
        this.userId = userId;
    }
    public void deposit() {
        if (canDeposit()) {
            totalCash += 100;
            if (isFull()) {
                state = FULL;
            } else {
                state = NON_EMPTY;
            }
        } else {
            throw new IllegalStateException("The deposit method can not be invoked!");
        }
    }

    public void withdraw() {
        if (canWithDraw()) {
            totalCash -= 100;
            if (totalCash <= 0) {
                state = EMPTY;
            } else {
                state = NON_EMPTY;
            }
        } else {
            throw new IllegalStateException("The withdraw method can not be invoked!");
        }
    }

    public void recycle() {
        if (canRecycle()) {
            state = RECYCLED;
        } else {
            throw new IllegalStateException("The recycle method can not be invoked!");
        }
    }

    public int getTotalCash() {
        return totalCash;
    }

    public String getState() {
        return state;
    }

    public int getUserId() {
        return userId;
    }

    private boolean isFull() {
        return totalCash == 500;
    }

    private boolean canDeposit() {
        return totalCash < 500 && (state == EMPTY || state == NON_EMPTY);
    }

    private boolean canWithDraw() {
        return totalCash >= 100 && (state == FULL || state == NON_EMPTY);
    }

    private boolean canRecycle() {
        return totalCash == 0 && state == EMPTY;
    }
}
