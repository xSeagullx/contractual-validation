package com.xseagullx.immutable;

public class NestingDomainWithWritable extends NestingDomain implements HasWithWritable<NestingDomainWithWritable> {
	public NestingDomainWithWritable(String value) {
		super(value);
	}
}
