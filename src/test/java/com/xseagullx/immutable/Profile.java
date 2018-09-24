package com.xseagullx.immutable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.time.LocalDate;
import java.util.Objects;

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

@Value
class OptionalField {
	String value;
}

public class Profile implements HasWithWritable<Profile> {
	@Getter @Setter private String firstName;
	@Getter @Setter private String lastName;
	@Getter @Setter private LocalDate dob;
	@Getter @Setter private Address address;
	@Getter @Setter private PhoneNumber phoneNumber;
	@Setter private OptionalField optionalField;

	public static Contract<Profile, String> profileContract = new ContractBuilder<Profile, String>()
			// watch out, we can't use raw field access here, as it'll ask for field from proxy
			.rule("firstName", (Profile it) -> it.getFirstName() == null ? "firstName can't be null" : null)
			.rule("lastName", (Profile it) -> it.getLastName() == null ? "lastName can't be null" : null)
			.rule("phoneNumber", (Profile it) -> it.getPhoneNumber() == null ? "phoneNumber can't be null" : null)
			// Q: Should we trigger other validation, if one fails (should we build a graph, and validate based on it. Cause otherwise we can't guarantee validation order. And should write our complex validations defensively (repeating higher level validation))
			.rule("phoneNumber", "address.country", (Profile it) -> !Objects.equals(it.getPhoneNumber() != null ? it.getPhoneNumber().getCode() : null, it.getAddress().getCountry()) ? "phoneNumber.code should be == country.code" : null)
			.build();

	public OptionalField getOptionalFieldCustomGette() {
		return optionalField;
	}
}
