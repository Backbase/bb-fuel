# Capability data setup
- The following is available for ingestion, but by default disabled - configurable via property:
- Transactions (see description below)
- Approvals (see description below)
- Limits (see description below)
- Contacts with multiple accounts per user
- Payments per user
- Notifications on global target group
- Messages per user
- Actions per user for SEPA CT and/or US Wire arrangements
- Bill Pay enrolment per user

Note: This can be rerun on an existing environment which already contains data by setting the property `ingest.access.control` to `false`

## Transactions setup
- By default ingesting transactions is disabled - configurable via property
    - Only works if property `ingest.access.control` is set to `true` due to the required external arrangement id when ingesting transactions.
    - This external arrangement id is only available when creating an arrangement (part of the access control setup). The external arrangement id is currently not retrievable via any REST endpoint.
- If enabled, random transactions (by default: between 10 and 50) per arrangement per today's date
- Possible to use the PFM categories by setting the property `use.pfm.categories.for.transactions` to `true`

## Approvals setup
Approvals configuration can be ingested for payments and contacts based on master service agreements.

### Approval types
- Approval type A with rank 1
- Approval type B with rank 2
- Approval type C with rank 3

### Payments: policies
- Zero approval policy with upper bound of 100
- Policy with approval type A (1 approval required) with upper bound of 1000
- Policy with approval types A + B (for each 1 approval required) with upper bound of 100,000
- Policy with approval types A + B + C (for each 1 approval required) unbounded

In case of less than 3 users under a legal entity as defined in [legal-entities-with-users.json](../src/main/resources/data/legal-entities-with-users.json), only a zero approval policy unbounded is applied

### Payments: users
- For each set of 4 users (sorted by name ASC) per legal entity defined in [legal-entities-with-users.json](../src/main/resources/data/legal-entities-with-users.json) will have the following setup:
- User no. 1: approval type A
- User no. 2: approval type B
- User no. 3: approval type C
- User no. 4: approval types A, B, C
- etc.

Example:
- User no. 5: approval type A
- User no. 6: approval type B
- User no. 7: approval type C
- User no. 8: approval types A, B, C
- User no. 9: approval type A
- User no. 10: approval type B

In case of less than 3 users under a legal entity as defined in [legal-entities-with-users.json](../src/main/resources/data/legal-entities-with-users.json), no approval types will be assigned to users

Note: each approval type will be assigned to a separate (new) function group with the payments function.

### Contacts: policies
- Policy with approval type A (1 approval required)

### Contacts: users
- Each user with approval type A

Note: this approval type will be assigned to a separate (new) function group with the contacts function.

## Limits setup
Periodic limit per service agreement, for function group "Admin":

- Transactional limit with amount of 1,000,000
- Privileges "create" and "approve"
- Functions SEPA CT, US Domestic Wire and US Foreign Wire