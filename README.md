# Data loader

## Description
Data loader ingests the following:
- Legal entities
- Users
- Function groups
- Data groups
- Permissions
- Products
- Arrangements
- Transactions

## Entitlements setup
- Root legal entity with user `admin` as entitlements admin
- Legal entity (under the root legal entity) per user (this is the so-called retail user setup) - configurable, see section *Users setup*
- Function groups for every business function with all privileges
- Data group consisting of arrangements per user

All function groups and data groups are assigned to the user via master service agreement of the legal entity.

## Product summary setup
- 7 products: current account, savings account, debit card, credit card, loan, term deposit and investment account.
- Random arrangements (between 10 and 50) per legal entity

## Transactions setup
- Random transactions (between 100 and 300) per arrangement per today's date

## Users setup
- Out of the box only the following users are covered: `user`, `designer`, `manager`
- If more/other users are required, you can provide your own `users.json` file.

### How to use custom user list
1. Create json file named `users.json` custom user list with the following format.

Each `users` array consists of the users which will be ingested under one legal entity.

Example:
```javascript
[
  {
    "users": [
      "user"
    ]
  },
  {
    "users": [
      "designer"
    ]
  },
  {
    "users": [
      "manager"
    ]
  }
]
```
2. Place `users.json` in a folder named `data`

## How to run data loader
1. Provision an [Autoconfig](https://backbase.atlassian.net/wiki/x/94BtC) environment based on `dbs` or `dbs-microservices` stack with **at least** the following capabilities:
```
capabilities="Entitlements,ProductSummary,Transactions"
```
2. Run the data loader as follows:
```
java -Denvironment-name=your-env-00 -jar dataloader-jar-with-dependencies.jar
```
Note: It only works on a *clean* environment, in other words: an environment without any data ingested before.

### Run with custom user list
```
java -Denvironment-name=your-env-00 -cp /path/to/custom/resources/folder/:dataloader-jar-with-dependencies.jar com.backbase.testing.dataloader.Runner
```
`/path/to/custom/resources/folder/` must contain `data/users.json`

## Questions and issues
If you have a question about the Data loader, or are experiencing a problem please contact the author, Kwo Ding via Hipchat or [e-mail](mailto:kwo@backbase.com)

## Contributing
You are welcome to provide bug fixes and new features in the form of pull requests. If you'd like to contribute, please be mindful of the following guidelines:

- All changes should be tested on an Autoconfig environment based on the latest versions.
- Please make one change per pull request.
- Use descriptive commit messages which will be used for release notes.
- If the new feature is significantly large/complex/breaks existing behavior, please first post a summary of your idea to Kwo Ding, so a discussion can be held. This will avoid significant amounts of coding time spent on changes that ultimately get rejected.
- Try to avoid reformats of files that change the indentation, tabs to spaces etc., as this makes reviewing diffs much more difficult.