package com.axelor.apps.tool;

public class Pair<A, B> {
	private final A first;
	private final B second;

	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	public boolean equals(Object other) {
		if (other instanceof Pair) {
			Pair<?, ?> otherPair = (Pair<?, ?>) other;
			return (first == otherPair.first
					|| (first != null && otherPair.first != null && first.equals(otherPair.first)))
					&& (second == otherPair.second
							|| (second != null && otherPair.second != null && second.equals(otherPair.second)));
		}

		return false;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

}
