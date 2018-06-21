# Data loader

## Description
Data loader ingests the following:
- Legal entities
- Service agreements
- Users
- Function groups
- Data groups
- Permissions
- Products
- Arrangements
- Transactions
- Contacts
- Payments
- Notifications
- Conversations

### Access control setup
- Root legal entity with user `admin` as entitlements admin
- Legal entities (under the root legal entity `C000000`) per legal entity entry with users array in the files [legal-entities-with-users.json](src/main/resources/data/legal-entities-with-users.json) and [legal-entities-with-users-without-permissions.json](src/main/resources/data/legal-entities-with-users-without-permissions.json) - configurable, see section *Custom data*

For legal entities and users in the file [legal-entities-with-users.json](src/main/resources/data/legal-entities-with-users.json):
- 3 function groups for all business functions with all privileges per service agreement of the legal entity from the input file:
    1. One function group for business function "SEPA CT"
    2. One function group for business functions "US Domestic Wire" and "US Foreign Wire"
    3. One function group with all other business functions
- 3 data groups:
    1. EUR currency arrangements for function group for business function "SEPA CT"
    2. USD currency arrangements for function group for business functions "US Domestic Wire" and "US Foreign Wire"
    3. Random currency arrangements for the other function group

- All function groups and data groups are assigned to the users via master service agreement of the legal entities from the input file.

### Product summary setup
- Default products: [products.json](src/main/resources/data/products.json)
- 3 sets of random arrangements (by default: between 10 and 30) each set for each data group: [legal-entities-with-users.json](src/main/resources/data/legal-entities-with-users.json)
- In case of current account arrangements random debit cards (by default: between 3 and 10) are associated
- Additionally (by default disabled) possible to ingest balance history based on a weekly balance history items for the past year
    - Only works if property `ingest.access.control` is set to `true` due to the required external arrangement id when ingesting balance history items.
    - This external arrangement id is only available when creating an arrangement (part of the access control setup). The external arrangement id is currently not retrievable via any REST endpoint.

### Transactions setup
- By default ingesting transactions is disabled - configurable via property
    - Only works if property `ingest.access.control` is set to `true` due to the required external arrangement id when ingesting transactions.
    - This external arrangement id is only available when creating an arrangement (part of the access control setup). The external arrangement id is currently not retrievable via any REST endpoint.
- If enabled, random transactions (by default: between 10 and 50) per arrangement per today's date
- Possible to use the PFM categories by setting the property `use.pfm.categories.for.transactions` to `true`

### Service agreements setup
Default service agreements (each object represents one service agreement): [serviceagreements.json](src/main/resources/data/serviceagreements.json)
- Legal entity ids will be retrieved via the external user ids given in the json file to set up the service agreements.
- Same access control setup for function/data groups and permissions for each service agreement, taking into account:
- For each participant that shares accounts, arrangements are ingested under its legal entity
- Function/data groups will be ingested under each service agreement
- Each participant that shares users are assigned permissions

### Users setup
By default only the following users are covered:
- Users with permissions as described under *Entitlements setup* and *Product summary setup*: [legal-entities-with-users.json](src/main/resources/data/legal-entities-with-users.json)
- Users without permissions under its own legal entity (no master service agreement, function and data groups associated): [legal-entities-with-users-without-permissions.json](src/main/resources/data/legal-entities-with-users-without-permissions.json)

If more/other users are required, you can provide your own `json` files, see *Custom data*.

### Capability data setup
- The following is available for ingestion, but by default disabled - configurable via property:
- Contacts with multiple accounts per user
- Payments per user
- Notifications on global target group
- Conversations per user

Note: This can be rerun on an existing environment which already contains data by setting the property `ingest.access.control` to `false`

## How to run data loader
1. Provision an [Autoconfig](https://backbase.atlassian.net/wiki/x/94BtC) environment based on `dbs` or `dbs-microservices` stack with **at least** the following capabilities (based on default configuration:
```
capabilities="Entitlements,ProductSummary"
```
2. Run the data loader as follows:

- For Autoconfig environments:
```
java -Denvironment.name=your-env-00 -jar dataloader-jar-with-dependencies.jar
```
- Or alter the [environment.properties](src/main/resources/environment.properties) accordingly

- For local Blade environment:
```
java -Duse.local.configurations=true -jar dataloader-jar-with-dependencies.jar
```
- See [local.properties](src/main/resources/local.properties) for this local configuration

## Properties for ingesting data
The following properties can be set to custom values for different purposes: [data.properties](src/main/resources/data.properties)

Example:
```
java -Denvironment.name=your-env-00 -Darrangements.max=20 -Ddebit.cards.min=10 -Ddebit.cards.max=30 -Dtransactions-max=50 -jar dataloader-jar-with-dependencies.jar
```

### Health check
Note: By default disabled
There is a built-in health check available to check whether services are up and running before ingesting. Available for:
- Entitlements
- Product summary
- Transactions

Set the value of the following property greater than 0 and it will check for that amount of minutes max.

Example:
```
healthcheck.timeout.in.minutes=10
```

### Note when running on environments with existing data
- No data will be removed from the environment
- It will check whether the following already exist, and if so, it will skip ingesting the existing item
    - Legal entities
    - Users
    - Products
- In case of existing users and master service agreements: it will try to re-use function groups based on business function
- When an existing function group is found, be aware that the privileges of the existing function group will remain in tact
- In case of custom service agreements, function groups are not re-used (new ones will be created)
- All other items will be added on top of the existing data
- However, if the external id of the existing root legal entity is not equal to `C000000`, this will fail the ingestion

## Custom data
See [Custom data](docs/CUSTOM_DATA.md)

## Questions and issues
If you have a question about the Data loader, or are experiencing a problem please contact the author, Kwo Ding via Hipchat or [e-mail](mailto:kwo@backbase.com)

## Contributing
You are welcome to provide bug fixes and new features in the form of pull requests. If you'd like to contribute, please be mindful of the following guidelines:

- All changes should be tested on an Autoconfig environment based on the latest versions.
- Please make one change per pull request.
- Use descriptive commit messages which will be used for release notes.
- Try to avoid reformats of files that change the indentation, tabs to spaces etc., as this makes reviewing diffs much more difficult.
