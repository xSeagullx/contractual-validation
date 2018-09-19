package com.xseagullx.immutable

import spock.lang.Specification

class ValidationSpec extends Specification {
	def domainFactory = new DomainFactory()

	void "contract is being enforced after withWritable"() {
		given:
			def contract = Mock(Contract)
			def domain = domainFactory.toImmutable(new TestDomain(), contract)
		when:
			domain.withWritable({})
		then:
			1 * contract.enforce(_) >> { DomainState it ->
				assert it.domainRoot == domain
				assert it.touchedProperties == new HashSet<>()
			}
	}

	void "contract is knows about updated properties"() {
		given:
			def contract = Mock(Contract)
			def domain = domainFactory.toImmutable(new TestDomain(), contract)
		when:
			domain.withWritable({
				it.setFirstName("abc")
				it.setFirstName("abc")
			})
		then:
			1 * contract.enforce(_) >> { DomainState it ->
				assert it.domainRoot == domain
				assert it.touchedProperties == ["firstName"].toSet()
			}
	}

	void "contract is knows about updated nesting properties"() {
		given:
			def contract = Mock(Contract)
			def domain = domainFactory.toImmutable(new NestedDomain(nesting: new NestingDomain("Hello")), contract)
		when:
			domain.withWritable({
				it.getNesting().value = "123"
			})
		then:
			1 * contract.enforce(_) >> { DomainState it ->
				assert it.domainRoot == domain
				assert it.touchedProperties == ["nesting.value"].toSet()
			}
	}

	void "if nesting object is HasWithWritable we can withWritable it, but it'll still use outer object as domainRoot"() {
		given:
			def contract = Mock(Contract)
			def domain = domainFactory.toImmutable(new NestedDomain(nesting: new NestingDomainWithWritable("Hello")), contract)
		when:
			(domain.getNesting() as HasWithWritable<NestingDomainWithWritable>).withWritable({ it ->
				it.value = "abc"
			})
		then:
			1 * contract.enforce(_) >> { DomainState it ->
				assert it.domainRoot == domain
				assert it.touchedProperties == ["nesting.value"].toSet()
			}
	}
}
