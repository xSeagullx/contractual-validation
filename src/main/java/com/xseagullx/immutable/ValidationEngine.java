package com.xseagullx.immutable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
class Rule <D, R> {
	private final String field;
	private final Function<D, R> check;
}
