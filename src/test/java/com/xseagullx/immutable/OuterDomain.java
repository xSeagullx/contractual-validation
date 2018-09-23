package com.xseagullx.immutable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OuterDomain implements HasWithWritable<OuterDomain> {
	String outerValue;
	InnerDomain innerDomain;
}

