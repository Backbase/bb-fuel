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

## Entitlements setup
- Root legal entity with user `admin` as entitlements admin
- Legal entity (under the root legal entity) per user array in the files [users.json](src/main/resources/data/users.json) and [users-without-permissions.json](src/main/resources/data/users-without-permissions.json) - configurable, see section *Custom data*

For users in the file [users.json](src/main/resources/data/users.json):
- Function groups for every business function with all privileges
- Data group consisting of arrangements per user
- All function groups and data groups are assigned to the user via master service agreement of the legal entity.

## Product summary setup
- 7 products: current account, savings account, debit card, credit card, loan, term deposit and investment account.
- Random arrangements (between 10 and 50) per legal entity of these users: [users.json](src/main/resources/data/users.json)
- In case of current account arrangements random debit cards (between 3 and 10) are associated

## Transactions setup
- Random transactions (between 10 and 50) per arrangement per today's date

## Service agreements setup
By default only one custom service agreement: [serviceagreements.json](src/main/resources/data/serviceagreements.json)
- Legal entity ids will be retrieved via the external user ids given in the json file to set up the service agreements.
- All function groups and data groups related to the legal entities of the consumers will be exposed to the service agreements.

## Users setup
By default only the following users are covered:
- Users with permissions as described under *Entitlements setup* and *Product summary setup*: [users.json](src/main/resources/data/users.json)
- Users without permissions under its own legal entity (no master service agreement, function and data groups associated): [users-without-permissions.json](src/main/resources/data/users-without-permissions.json)

If more/other users are required, you can provide your own `json` files, see *Custom data*.

## How to run data loader
1. Provision an [Autoconfig](https://backbase.atlassian.net/wiki/x/94BtC) environment based on `dbs` or `dbs-microservices` stack with **at least** the following capabilities:
```
capabilities="Entitlements,ProductSummary,Transactions"
```
2. Run the data loader as follows:
```
java -Denvironment.name=your-env-00 -jar dataloader-jar-with-dependencies.jar
```
Note: It only works on a *clean* environment, in other words: an environment without any data ingested before.

## Custom data

### Run with custom data
```
java -Denvironment.name=your-env-00 -cp /path/to/custom/resources/folder/:dataloader-jar-with-dependencies.jar com.backbase.testing.dataloader.Runner
```
`/path/to/custom/resources/folder/` must contain the custom `json` files

### How to create custom data
Example for the `users.json` (other files are: `users-without-permissions.json` and `serviceagreements.json`):

1. Create json file named `users.json` custom user list conforming existing format (in this case conforming: [users.json](src/main/resources/data/users.json))

Each `externalUserIds` array consists of the users which will be ingested under one legal entity.

Example:
```javascript
[
  {
    "externalUserIds": [
      "U99990001"
    ]
  },
  {
    "externalUserIds": [
      "U99990002",
      "U99990003"
    ]
  },
  {
    "externalUserIds": [
      "U99990004",
      "U99990005",
      "U99990006"
    ]
  }
]
```
2. Place `users.json` in a folder named `data`

Note: it is also possible to name/place the json file differently with the specific properties: `users.json.location`, `users.without.permissions.json.location` and `serviceagreements.json.location`

## Questions and issues
If you have a question about the Data loader, or are experiencing a problem please contact the author, Kwo Ding via Hipchat or [e-mail](mailto:kwo@backbase.com)

## Contributing
You are welcome to provide bug fixes and new features in the form of pull requests. If you'd like to contribute, please be mindful of the following guidelines:

- All changes should be tested on an Autoconfig environment based on the latest versions.
- Please make one change per pull request.
- Use descriptive commit messages which will be used for release notes.
- If the new feature is significantly large/complex/breaks existing behavior, please first post a summary of your idea to Kwo Ding, so a discussion can be held. This will avoid significant amounts of coding time spent on changes that ultimately get rejected.
- Try to avoid reformats of files that change the indentation, tabs to spaces etc., as this makes reviewing diffs much more difficult.