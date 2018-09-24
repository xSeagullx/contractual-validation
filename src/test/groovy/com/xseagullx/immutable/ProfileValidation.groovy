package com.xseagullx.immutable

import spock.lang.Specification

class ProfileValidation extends Specification {
	void "firstName is not null"() {
		def errors = []
		Profile profile = new DomainFactory().toImmutable(new Profile(), Profile.profileContract, { if (it != null) errors.add(it) })
		when:
			profile.withWritable {
				it.firstName = "hey"
			}
		then:
			errors == []
		when:
			profile.withWritable {
				it.firstName = null
			}
		then:
			errors == ["firstName can't be null"]
	}

	void "phone number code is validated"() {
		def errors = []
		Profile profile = new DomainFactory().toImmutable(new Profile(phoneNumber: new PhoneNumber("gb", "123"), address: new Address("street", "gb")), Profile.profileContract, {
			if (it != null) errors.add(it)
		})
		when:
			profile.withWritable {
				it.address.country = "fr"
			}
		then:
			errors == ["phoneNumber.code should be == country.code"]
	}

	void "phone number code is validated when phoneNumber has changed"() {
		def errors = []
		Profile profile = new DomainFactory().toImmutable(new Profile(phoneNumber: new PhoneNumber("gb", "123"), address: new Address("street", "gb")), Profile.profileContract, {
			if (it != null) errors.add(it)
		})
		when:
			profile.withWritable {
				it.phoneNumber = null
			}
		then:
			errors == ["phoneNumber can't be null", "phoneNumber.code should be == country.code"]
	}

	void "phone number code is validated when address itself has changed"() {
		def errors = []
		Profile profile = new DomainFactory().toImmutable(new Profile(phoneNumber: new PhoneNumber("gb", "123"), address: new Address("street", "gb")), Profile.profileContract, {
			if (it != null) errors.add(it)
		})
		when:
			profile.withWritable {
				it.address = new Address("street", "fr")
			}
		then:
			errors == ["phoneNumber.code should be == country.code"]
	}

	void "it is possible to trigger validation for the whole object"() {
		Set<String> errors = []
		ErrorHandlingStrategy<String> errorHandlinStrategy = { String it ->
			if (it != null) errors.add(it)
		}
		Profile profile = new DomainFactory().toImmutable(new Profile(phoneNumber: new PhoneNumber("gb", "123"), address: new Address("street", "fr")), Profile.profileContract, errorHandlinStrategy)
		when:
			Profile.profileContract.enforce(profile, errorHandlinStrategy)
		then:
			errors == ["firstName can't be null", "lastName can't be null", "phoneNumber.code should be == country.code"].toSet()
	}
}
