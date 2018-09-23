package com.xseagullx.immutable;

public interface ErrorHandlingStrategy<T> {
	void handle(T validationResult);
}
