package com.xseagullx.immutable;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** T - Domain type. E - Error type */
@RequiredArgsConstructor
public class Contract<T, E> {
	private final List<Rule<T,E>> rules;

	/**
	 * Validates doiman against given contract.
	 * Runs all validation rules.
	 */
	public void enforce(T domain, ErrorHandlingStrategy<E> errorHandlingStrategy) {
		rules
				.stream()
				.map(Rule::getCheck)
				.collect(Collectors.toSet())
				.forEach(it -> errorHandlingStrategy.handle(it.apply(domain)));
	}

	/**
	 * Validates doiman against given contract.
	 * Runs validation rules for touched fields.
	 */
	void enforce(DomainState<T, E> domainState) {
		Set<Function> checkedRules = new HashSet<>();
		domainState
				.getTouchedProperties()
				.stream()
				.flatMap(this::getRules)
				.map(Rule::getCheck)
				.filter(it -> !checkedRules.contains(it))
				.map(it -> { checkedRules.add(it); return it; })
				.map(it -> it.apply(domainState.getDomainRoot()))
				.forEach(domainState.getErrorHandlingStrategy()::handle);
	}

	private Stream<Rule<T, E>> getRules(String touchedProperty) {
		return rules.stream().filter(it -> it.getField().startsWith(touchedProperty));
	}
}
