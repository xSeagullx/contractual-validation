package com.xseagullx.immutable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NestedDomain implements HasWithWritable<NestedDomain> {
	NestingDomain nesting;
}

