package com.xseagullx.immutable

import spock.lang.Specification

class ValidationSpec extends Specification {
	def domainFactory = new DomainFactory()

	void "contract is being enforced after withWritable"() {
		given:
			def contract = Mock(Contract)
			def domain = domainFactory.toImmutable(new TestDomain(), contract, null)
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
			def domain = domainFactory.toImmutable(new TestDomain(), contract, null)
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
			def domain = domainFactory.toImmutable(new OuterDomain(innerDomain: new InnerDomain("Hello")), contract, null)
		when:
			domain.withWritable({
				it.getInnerDomain().value = "123"
			})
		then:
			1 * contract.enforce(_) >> { DomainState it ->
				assert it.domainRoot == domain
				assert it.touchedProperties == ["innerDomain.value"].toSet()
			}
	}

	void "if nesting object is HasWithWritable we can withWritable it, but it'll still use outer object as domainRoot"() {
		given:
			def contract = Mock(Contract)
			def domain = domainFactory.toImmutable(new OuterDomain(innerDomain: new NestingDomainWithWritable("Hello")), contract, null)
		when:
			(domain.getInnerDomain() as HasWithWritable<NestingDomainWithWritable>).withWritable({ it ->
				it.value = "abc"
			})
		then:
			1 * contract.enforce(_) >> { DomainState it ->
				assert it.domainRoot == domain
				assert it.touchedProperties == ["innerDomain.value"].toSet()
			}
	}
}
