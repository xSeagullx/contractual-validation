package com.xseagullx.immutable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ImmutableDomain
public class TestDomain implements Immutable<TestDomain> {
	String firstName;
}
