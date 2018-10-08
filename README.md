# bb-fuel

## Description
bb-fuel can ingest the following:
- Legal entities
- Service agreements
- Users
- Function groups
- Data groups
- Permissions
- Products
- Arrangements
- Transactions
- Approvals
- Contacts
- Payments
- Notifications
- Messages
- Actions

It is based on REST and relies on DBS service specs.

## Data setup (read before running)
- [Base setup](docs/BASE_SETUP.md)
- [Capability data setup](docs/CAPABILITY_DATA_SETUP.md)

## How to run bb-fuel
All builds can be found [here](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel)

1. Provision an [Autoconfig](https://backbase.atlassian.net/wiki/x/94BtC) environment based on `dbs` or `dbs-microservices` stack with **at least** the following capabilities (based on default configuration:
```
capabilities="Entitlements,ProductSummary"
```
2. Run bb-fuel as follows:

- For Autoconfig environments:
```
java -Denvironment.name=your-env-00 -jar bb-fuel-{version}-boot.jar
```
- Or alter the [environment.properties](src/main/resources/environment.properties) accordingly

- For local Blade environment:
```
java -Duse.local.configurations=true -jar bb-fuel-{version}-boot.jar
```
- See [local.properties](src/main/resources/local.properties) for this local configuration

## Properties for ingesting data
The following properties can be set to custom values for different purposes: [data.properties](src/main/resources/data.properties)

Example:
```
java -Denvironment.name=your-env-00 -Darrangements.max=20 -Ddebit.cards.min=10 -Ddebit.cards.max=30 -Dtransactions-max=50 -jar bb-fuel-{version}-boot.jar
```

### Health check
Note: By default disabled
There is a built-in health check available to check whether services are up and running before ingesting. Available for:
- Access control
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
- In case of existing users and service agreements: it will try to re-use function groups based on function name and service agreement combination
- When an existing function group is found, be aware that the privileges of the existing function group will remain in tact
- All other items will be added on top of the existing data
- However, if the external id of the existing root legal entity is not equal to `C000000`, this will fail the ingestion

## Custom data
See [Custom data](docs/CUSTOM_DATA.md)

## Versions
bb-fuel supports multiple DBS versions. See below which version maps to the required DBS version.

| DBS version | Tool minimal version                                                                                             |
|-------------|------------------------------------------------------------------------------------------------------------------|
| 2.13.0      | [1.6.0+](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel/1.6.0/)           |
| 2.12.2      | [1.4.11+](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel/1.4.11/)         |
| 2.12.1      | [1.4.5+](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel/1.4.5/)           |
| 2.12.0      | [1.3.6+](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel/1.3.6/)           |
| 2.11.2      | [1.3.5+](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel/1.3.5/)           |
| 2.11.1      | [1.1.0+](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel/1.1.0/)           |
| 2.11.0      | [1.0.5+](https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bb-fuel/1.0.5/)           |
| 2.10.4      | [2.10.4 tag on repo](https://stash.backbase.com/projects/CT/repos/bb-fuel/commits?until=refs%2Ftags%2F2.10.4) |
| 2.10.3      | [2.10.3 tag on repo](https://stash.backbase.com/projects/CT/repos/bb-fuel/commits?until=refs%2Ftags%2F2.10.3) |
| 2.10.2      | [2.10.2 tag on repo](https://stash.backbase.com/projects/CT/repos/bb-fuel/commits?until=refs%2Ftags%2F2.10.2) |
| 2.10.1      | [2.10.1 tag on repo](https://stash.backbase.com/projects/CT/repos/bb-fuel/commits?until=refs%2Ftags%2F2.10.1) |
| 2.10.0      | [2.10.0 tag on repo](https://stash.backbase.com/projects/CT/repos/bb-fuel/commits?until=refs%2Ftags%2F2.10.0) |

## Questions and issues
If you have a question about bb-fuel, or are experiencing a problem please contact the author, Kwo Ding via Hipchat or [e-mail](mailto:kwo@backbase.com)

## Contributing
You are welcome to provide bug fixes and new features in the form of pull requests. If you'd like to contribute, please be mindful of the following guidelines:

- All changes should be tested on an Autoconfig environment based on the latest versions.
- Please make one change per pull request.
- Use descriptive commit messages which will be used for release notes.
- Try to avoid reformats of files that change the indentation, tabs to spaces etc., as this makes reviewing diffs much more difficult.
