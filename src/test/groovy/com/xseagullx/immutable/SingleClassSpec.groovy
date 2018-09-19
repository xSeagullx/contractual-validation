package com.xseagullx.immutable

import spock.lang.Specification

class SingleClassSpec extends Specification {
	def domainFactory = new DomainFactory()

	void "wrapped classes implement Immutable marker interface" () {
		expect:
			domainFactory.toImmutable(new TestDomain()) instanceof Immutable
	}

	void "withWritable does not work for naked instances" () {
		when:
			new TestDomain().withWritable(null)
		then:
			thrown ClassIsNowWrappedException
	}

	void "setters are throwing exception by default" () {
		when:
			domainFactory.toImmutable(new TestDomain()).setFirstName("Blah")
		then:
			thrown DomainIsReadOnlyException
	}

	void "getters are ok to call" () {
		TestDomain domain = domainFactory.toImmutable(new TestDomain(firstName: "Blah"))
		expect:
			domain.getFirstName() == "Blah"
	}

	void "withWritable allows to call setters" () {
		TestDomain domain = domainFactory.toImmutable(new TestDomain(firstName: "Blah"))
		when:
			domain.withWritable { it.setFirstName("Blah-Blah") }
		then:
			domain.firstName == "Blah-Blah"
	}

	void "withWritable returns immutable object" () {
		TestDomain domain = domainFactory.toImmutable(new TestDomain(firstName: "Blah"))
		expect:
			domain.withWritable { it.setFirstName("Blah-Blah") } is domain
	}

	void "inside withWritable we can get the set of touched fields" () {
		TestDomain domain = domainFactory.toImmutable(new TestDomain(firstName: "Blah"))
		expect:
			domain.withWritable {
				it.setFirstName("Blah-Blah")
				assert (it as Validating).touchedProperties == ["firstName"].toSet()
			}
	}

	// todo validate
	// todo hydrate
}
