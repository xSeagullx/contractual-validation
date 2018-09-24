package com.xseagullx.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OuterOuterDomain implements HasWithWritable<OuterOuterDomain> {
	private OuterDomain outerDomain;
}
