package com.xseagullx.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Getter
@Setter
@AllArgsConstructor
class Address {
	private String street;
	private String country;
}

@Getter
@Setter
@AllArgsConstructor
class PhoneNumber {
	String code;
	String number;
}

@Getter @Setter
public class Profile implements HasWithWritable<Profile> {
	private String firstName;
	private String lastName;
	private LocalDate dob;
	private Address address;
	private PhoneNumber phoneNumber;

	public static Contract<Profile, String> profileContract = new ContractBuilder<Profile, String>()
			// watch out, we can't use raw field access here, as it'll ask for field from proxy
			.rule("firstName", (Profile it) -> it.getFirstName() == null ? "firstName can't be null" : null)
			.rule("lastName", (Profile it) -> it.getLastName() == null ? "lastName can't be null" : null)
			.rule("phoneNumber", (Profile it) -> it.getPhoneNumber() == null ? "phoneNumber can't be null" : null)
			// Q: Should we trigger other validation, if one fails (should we build a graph, and validate based on it. Cause otherwise we can't guarantee validation order. And should write our complex validations defensively (repeating higher level validation))
			.rule("phoneNumber", "address.country", (Profile it) -> !Objects.equals(it.getPhoneNumber() != null ? it.getPhoneNumber().getCode() : null, it.getAddress().getCountry()) ? "phoneNumber.code should be == country.code" : null)
			.build();
}

class ContractBuilder<T, E> {
	private final List<Rule<T, E>> rules = new ArrayList<>();

	ContractBuilder<T, E> rule(String field, Function<T, E> function) {
		rules.add(new Rule<>(field, function));
		return this;
	}

	ContractBuilder<T, E> rule(String field1, String field2, Function<T, E> function) {
		rules.add(new Rule<>(field1, function));
		rules.add(new Rule<>(field2, function));
		return this;
	}

	Contract<T, E> build() {
		return new Contract<>(rules);
	};
}
