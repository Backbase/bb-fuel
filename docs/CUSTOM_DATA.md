# Custom data

## Run with custom data
```
java -Denvironment.name=your-env-00 -cp /path/to/custom/resources/folder/:dataloader-jar-with-dependencies.jar com.backbase.testing.dataloader.Runner
```
`/path/to/custom/resources/folder/` must contain the custom `json` files

## How to create custom data
Example for the `legal-entities-with-users.json` (other files are: `legal-entities-with-users-without-permissions.json`, `serviceagreements.json` and `products.json`):

1. Create json file named `legal-entities-with-users.json` with custom legal entities and assigned custom user list conforming existing format (in this case conforming: [legal-entities-with-users.json ](src/main/resources/data/legal-entities-with-users.json ))

By default if customizable fields have not been provided, system will generate randomized values for it.
Optional fields in the data structure:
- `legalEntityExternalId`
- `parentLegalEntityExternalId`
- `legalEntityName`
- `legalEntityType`

Mandatory fields:
- `userExternalIds` - array of Strings, representing User ids.

Each `userExternalIds` array consists of the users which will be ingested under the above legal entity.

Example:
```javascript
[
  {
    "legalEntityExternalId": "LE000002",
    "parentLegalEntityExternalId": "C000000",
    "legalEntityName": "Hong Kong Legal Entity",
    "legalEntityType": "CUSTOMER",
    "userExternalIds": [
      "U0091011",
      "U0091012",
      "U0091013",
      "U0091014",
      "U0091015"
    ]
  }
]
```
2. Place `legal-entities-with-users.json` in a folder named `data`

Note: it is also possible to name/place the json file differently with the specific properties: `legal.entities.with.users.json.location`, `legal.entities.with.users.without.permissions.json.location` and `serviceagreements.json.location`
