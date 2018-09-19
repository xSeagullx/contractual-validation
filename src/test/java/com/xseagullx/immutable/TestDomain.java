package com.xseagullx.immutable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestDomain implements HasWithWritable<TestDomain> {
	String firstName;
}
