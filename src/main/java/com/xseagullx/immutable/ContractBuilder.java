package com.xseagullx.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ContractBuilder<T, E> {
	private final List<Rule<T, E>> rules = new ArrayList<>();

	public ContractBuilder<T, E> rule(String field, Function<T, E> function) {
		rules.add(new Rule<>(field, function));
		return this;
	}

	public ContractBuilder<T, E> rule(String field1, String field2, Function<T, E> function) {
		rules.add(new Rule<>(field1, function));
		rules.add(new Rule<>(field2, function));
		return this;
	}

	public ContractBuilder<T, E> rule(String field1, String field2, String field3, Function<T, E> function) {
		rules.add(new Rule<>(field1, function));
		rules.add(new Rule<>(field2, function));
		rules.add(new Rule<>(field3, function));
		return this;
	}

	public Contract<T, E> build() {
		return new Contract<>(rules);
	}
}
