package com.xseagullx.immutable

import spock.lang.Specification
import spock.lang.Unroll

class ContractSpec extends Specification {
	def errorHandlingStrategy = Mock(ErrorHandlingStrategy)

	void "single field validation. not touched"() {
		given:
			def rule = new Rule<>("firstName", { TestDomain domain -> domain.firstName == "Abc" ? Optional.of("Abc is not a name") : Optional.empty() })

			def domain = new TestDomain(firstName: "Pavel")
			def contract = new Contract<TestDomain, Optional<String>>([rule])
		when:
			contract.enforce(domainState(contract, domain, "lastName"))
		then:
			0 * errorHandlingStrategy.handle(_)
	}

	void "single field validation. no error"() {
		given:
			def rule = new Rule<>("firstName", { TestDomain domain -> domain.firstName == "Abc" ? Optional.of("Abc is not a name") : Optional.empty() })

			def domain = new TestDomain(firstName: "Pavel")
			def contract = new Contract<TestDomain, Optional<String>>([rule])
		when:
			contract.enforce(domainState(contract, domain, "firstName"))
		then:
			1 * errorHandlingStrategy.handle(Optional.empty())
	}

	void "single field validation. error"() {
		given:
			def rule = new Rule<>("firstName", { TestDomain domain -> domain.firstName == "Abc" ? Optional.of("Abc is not a name") : Optional.empty() })

			def domain = new TestDomain(firstName: "Abc")
			def contract = new Contract<TestDomain, Optional<String>>([rule])
		when:
			contract.enforce(domainState(contract, domain, "firstName"))
		then:
			1 * errorHandlingStrategy.handle(Optional.of("Abc is not a name"))
	}

	void "single field, multiple error validation. Single error"() {
		given:
			def rule1 = new Rule<>("firstName", { TestDomain domain -> domain.firstName.length() <= 1 ? Optional.of("FirstName should be longer than 1 character") : Optional.empty() })
			def rule2 = new Rule<>("firstName", { TestDomain domain -> domain.firstName == "Abc" ? Optional.of("Abc is not a name") : Optional.empty() })

			def domain = new TestDomain(firstName: "Abc")
			def contract = new Contract<TestDomain, Optional<String>>([rule1, rule2])
		when:
			contract.enforce(domainState(contract, domain, "firstName"))
		then:
			1 * errorHandlingStrategy.handle(Optional.empty())
			1 * errorHandlingStrategy.handle(Optional.of("Abc is not a name"))
	}

	@Unroll
	void "nested field validation is being triggered for #touchedProperties"() {
		given:
			def check = { OuterDomain it -> it.outerValue == it.innerDomain.value ? "test" : null }
			def rule1 = new Rule("outerValue", check)
			def rule2 = new Rule("innerDomain", check)
			def rule3 = new Rule("innerDomain.value", check)
			def domain = new OuterDomain(outerValue: "foo", innerDomain: new InnerDomain("foo"))

			def contract = new Contract<OuterDomain, String>([rule1, rule2, rule3])
		when:
			contract.enforce(domainState(contract, domain, *touchedProperties))
		then:
			1 * errorHandlingStrategy.handle("test")
		where:
			touchedProperties << [["outerValue"], ["innerDomain"], ["innerDomain.value"], ["outerValue", "innerDomain"], ["outerValue", "innerDomain.value"]]
	}

	private <T> DomainState domainState(Contract contract, T domain, String ... touchedProperties) {
		def domainState = new DomainState(contract, errorHandlingStrategy)
		domainState.domainRoot = domain
		domainState.touchedProperties.addAll(touchedProperties)
		domainState
	}
}
