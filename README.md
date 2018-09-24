# Library to control domain mutability and to validate it based on a given contract.

## Behaviour
1. Domain validation is defined separately from the data. Validation rules are using current values of the domain fields.
2. Validation is being triggered automatically when domain is mutated. Mutations are encapsulated in a lambda, and are "atomic" from the viewpoint of contract. i.e. we only guarantee validity before the mutation and after the mutation, but not during the mutation.
3. Any setter access outside of withWritable should throw an exception.

## To think
1. How idea of "always having a correct domain" plays with the need to load domain from DB as-is (assuming we have data that is no longer valid).
2. If mutation is leading to contract being violated we might want to rollback the object. Is it really necessary (assuming user would not persist invalid domains)?
3. Should we continue validation of field, if another rule is already stating it being invalid?
4. Should we validate other fields, when they depend on invalid field?
