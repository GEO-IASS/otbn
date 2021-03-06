package inference;

import java.util.Objects;
/**
 * Created by Chao li on 10/23/16.
 */
public class Var implements Comparable<Var> {
    private final int index_;
    private int card_;

    public Var(int index_, int card_) {
        this.index_ = index_;
        this.card_ = card_;
    }

    // ~ Getters and Setters ~
    public int getIndex() {
        return index_;
    }

    public int getCard() {
        return card_;
    }

    protected void setCard(int card) {
        this.card_ = card;
    }

    // ~ Methods ~
    @Override
    public int compareTo(Var o) {
        return this.index_ - o.index_;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index_);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Var var = (Var) o;
        return index_ == var.index_;
    }
    @Override
    public String toString() {
        return "Var{" +
                "index_=" + index_ +
                ", card_=" + card_ +
                '}';
    }
}