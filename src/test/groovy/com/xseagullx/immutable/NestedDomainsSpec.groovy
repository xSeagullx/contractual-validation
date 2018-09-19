package com.xseagullx.immutable

import spock.lang.Specification

class NestedDomainsSpec extends Specification {
	private DomainFactory domainFactory = new DomainFactory()

	void "outer setter is not accessible"() {
		given:
			def domain = domainFactory.toImmutable(new NestedDomain(nesting: new NestingDomain("test")))
		when:
			domain.setNesting(new NestingDomain())
		then:
			thrown DomainIsReadOnlyException
	}

	void "nested setter is not accessible"() {
		given:
			def domain = domainFactory.toImmutable(new NestedDomain(nesting: new NestingDomain("test")))
		when:
			domain.getNesting().setValue("Hey")
		then:
			thrown DomainIsReadOnlyException
	}

	void "nested setter is accessible when withWritable is called"() {
		given:
			def domain = domainFactory.toImmutable(new NestedDomain(nesting: new NestingDomain("test")))
		when:
			domain.withWritable {
				it.getNesting().setValue("Hey")
			}

		then:
			domain.nesting.value == "Hey"
	}

	void "withWritable wraps newly created nesting class in proxy"() {
		given:
			def domain = domainFactory.toImmutable(new NestedDomain(nesting: new NestingDomain("test")))
		when:
			domain.withWritable {
				it.setNesting(new NestingDomain("lol"))
			}

		then:
			domain.nesting instanceof Immutable
	}

	void "withWritable collects nested domain updates"() {
		given:
			def domain = domainFactory.toImmutable(new NestedDomain(nesting: new NestingDomain("test")))
		when:
			domain.withWritable {
				it.setNesting(new NestingDomain("lol"))
				assert (it as Validating).touchedProperties == ['nesting'].toSet()
				it.nesting.value = "Hey"
				assert (it.nesting as Validating).touchedProperties == ['nesting', 'nesting.value'].toSet()
			}

		then:
			domain.nesting instanceof Immutable
	}
}
