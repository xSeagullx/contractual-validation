package com.xseagullx.immutable

import spock.lang.Specification

class NestedDomainsSpec extends Specification {
	private DomainFactory domainFactory = new DomainFactory()

	void "outer setter is not accessible"() {
		given:
			def domain = domainFactory.toImmutable(new OuterDomain(innerDomain: new InnerDomain("test")))
		when:
			domain.setInnerDomain(new InnerDomain())
		then:
			thrown DomainIsReadOnlyException
	}

	void "nested setter is not accessible"() {
		given:
			def domain = domainFactory.toImmutable(new OuterDomain(innerDomain: new InnerDomain("test")))
		when:
			domain.getInnerDomain().setValue("Hey")
		then:
			thrown DomainIsReadOnlyException
	}

	void "nested setter is accessible when withWritable is called"() {
		given:
			def domain = domainFactory.toImmutable(new OuterDomain(innerDomain: new InnerDomain("test")))
		when:
			domain.withWritable {
				it.getInnerDomain().setValue("Hey")
			}

		then:
			domain.innerDomain.value == "Hey"
	}

	void "withWritable wraps newly created nesting class in proxy"() {
		given:
			def domain = domainFactory.toImmutable(new OuterDomain(innerDomain: new InnerDomain("test")))
		when:
			domain.withWritable {
				it.setInnerDomain(new InnerDomain("lol"))
			}

		then:
			domain.innerDomain instanceof Immutable
	}

	void "withWritable collects nested domain updates"() {
		given:
			def domain = domainFactory.toImmutable(new OuterDomain(innerDomain: new InnerDomain("test")))
		when:
			domain.withWritable {
				it.setInnerDomain(new InnerDomain("lol"))
				assert (it as Validating).touchedProperties == ['nesting'].toSet()
				it.innerDomain.value = "Hey"
				assert (it.innerDomain as Validating).touchedProperties == ['nesting', 'nesting.value'].toSet()
			}

		then:
			domain.innerDomain instanceof Immutable
	}
}
