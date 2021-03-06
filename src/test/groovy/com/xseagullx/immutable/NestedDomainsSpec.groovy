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
				assert (it as Validating).touchedProperties == ['innerDomain'].toSet()
				it.innerDomain.value = "Hey"
				assert (it.innerDomain as Validating).touchedProperties == ['innerDomain', 'innerDomain.value'].toSet()
			}

		then:
			domain.innerDomain instanceof Immutable
	}

	void "recursive proxying works"() {
		given:
			def domain = domainFactory.toImmutable(new OuterOuterDomain(new OuterDomain(innerDomain: new InnerDomain("abc"))))
		expect:
			domain instanceof Immutable
			domain.outerDomain instanceof Immutable
			domain.outerDomain.innerDomain instanceof Immutable
		when:
			domain.outerDomain.innerDomain.value = "123"
		then:
			def exception = thrown(DomainIsReadOnlyException)
			exception.fieldName == "outerDomain.innerDomain.value"
	}
}
