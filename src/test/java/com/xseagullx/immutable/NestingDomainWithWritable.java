package com.xseagullx.immutable;

public class NestingDomainWithWritable extends InnerDomain implements HasWithWritable<NestingDomainWithWritable> {
	public NestingDomainWithWritable(String value) {
		super(value);
	}
}
