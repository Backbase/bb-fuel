# Base setup

## Access control setup
- Root legal entity with user `admin` as entitlements admin
- Legal entities (under the root legal entity `C000000`) per legal entity entry with users array in the files [legal-entities-with-users.json](src/main/resources/data/legal-entities-with-users.json) and [legal-entities-with-users-without-permissions.json](src/main/resources/data/legal-entities-with-users-without-permissions.json) - configurable, see section *Custom data*

For legal entities and users in the file [legal-entities-with-users.json](src/main/resources/data/legal-entities-with-users.json):
- 3 function groups for all business functions with all privileges per service agreement of the legal entity from the input file:
    1. One function group for business function "SEPA CT" and "Product Summary"
    2. One function group for business functions "US Domestic Wire", "US Foreign Wire" and "Product Summary"
    3. One function group with all other business functions
- 3 data groups:
    1. EUR currency arrangements for function group for business function "SEPA CT" and "Product Summary"
    2. USD currency arrangements for function group for business functions "US Domestic Wire", "US Foreign Wire" and "Product Summary"
    3. Random currency arrangements for the other function group

- All function groups and data groups are assigned to the users via master service agreement of the legal entities from the input file.

## Product summary setup
- Default products: [products.json](src/main/resources/data/products.json)
- 3 sets of random arrangements (by default: between 10 and 30) each set for each data group:
    1. EUR currency arrangements
    2. USD currency arrangements
    3. Random currency arrangements
- In case of current account arrangements random debit cards (by default: between 3 and 10) are associated
- Additionally (by default disabled) possible to ingest balance history based on a weekly balance history items for the past quarter
    - Only works if property `ingest.access.control` is set to `true` due to the required external arrangement id when ingesting balance history items.
    - This external arrangement id is only available when creating an arrangement (part of the access control setup). The external arrangement id is currently not retrievable via any REST endpoint.

## Service agreements setup
Default service agreements (each object represents one service agreement): [serviceagreements.json](src/main/resources/data/serviceagreements.json)
- Legal entity ids will be retrieved via the external user ids given in the json file to set up the service agreements.
- Same access control setup for function/data groups and permissions for each service agreement, taking into account:
- For each participant that shares accounts, arrangements are ingested under its legal entity
- Function/data groups will be ingested under each service agreement
- Each participant that shares users are assigned permissions

## Users setup
By default only the following users are covered:
- Users with permissions as described under *Access control setup* and *Product summary setup*: [legal-entities-with-users.json](src/main/resources/data/legal-entities-with-users.json)
- Users without permissions under its own legal entity (no master service agreement, function and data groups associated): [legal-entities-with-users-without-permissions.json](src/main/resources/data/legal-entities-with-users-without-permissions.json)

If more/other users are required, you can provide your own `json` files, see *Custom data*.
