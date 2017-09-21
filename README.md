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

### Entitlements setup
- Root legal entity with user `admin` as entitlements admin
- Legal entity (under the root legal entity) per user (this is the so-called retail user setup) - configurable, see section *Users setup*
- Function groups for every business function with all privileges
- Data group consisting of arrangements per user

All function groups and data groups are assigned to the user.

### Product summary setup
- Random arrangements (between 10 and 50) per user

### Transactions setup
- Random transactions (between 100 and 300) per arrangement per today's date

### Users setup
- Out of the box only the following users are covered: `user`, `designer`, `manager`
- If more/other users are required, you can provide your own `users.json` file.

#### How to use custom user list
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