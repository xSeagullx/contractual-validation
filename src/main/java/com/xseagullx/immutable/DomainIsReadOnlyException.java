package com.xseagullx.immutable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DomainIsReadOnlyException extends IllegalStateException {
	private final String fieldName;
}
