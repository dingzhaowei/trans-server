package com.ding.trans.server.model;

public class Pair<L, R> {

    private L left;

    private R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L left() {
        return left;
    }

    public R right() {
        return right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() * 127 + right.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isInstance(obj)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        Pair<L, R> p = (Pair<L, R>) obj;
        return left.equals(p.left) && right.equals(p.right);
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", left, right);
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        if (left == null || right == null) {
            throw new NullPointerException();
        }
        return new Pair<L, R>(left, right);
    }

}
