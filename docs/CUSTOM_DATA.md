# Custom data

## How to run with custom data
```
java -Denvironment.name=your-env-00 -cp /path/to/custom/resources/folder/:dataloader-jar-with-dependencies.jar com.backbase.testing.dataloader.Runner
```
`/path/to/custom/resources/folder/` must contain the custom `json` files

## Built-in custom data setup
### How to run with performance test data setup
Based on [gatling-performance-simulations](https://stash.backbase.com/projects/CT/repos/gatling-performance-simulations/browse):
```
java -Denvironment.name=your-env-00 -Dlegal.entities.with.users.json.location=data/performance-test-legal-entities-with-users-{retail or business}.json dataloader-{version}-boot.jar
```

### How to run on multi-tenancy environments
Based on [multi-tenancy LDAP configuration in Autoconfig environments](https://stash.backbase.com/projects/ANSIBLE/repos/cxp6-v2/browse/files/multitenancy.ldif)

```
java -Denvironment.name=your-env-00 \
-Dlegal.entities.with.users.json.location=data/multitenancy/tenant_a.json \
-Dmulti.tenancy.environment=true \
-Dtenant.id=tenant_a \
-Droot.entitlements.admin=user001_t1 \
dataloader-{version}-boot.jar
```

Repeat for each tenant according to the LDAP configuration as follows:

| tenant.id | root.entitlements.admin | legal.entities.with.users.json.location |
|-----------|-------------------------|-----------------------------------------|
| tenant_a  | user001_t1              | data/multitenancy/tenant_a.json         |
| tenant_b  | user006_t2              | data/multitenancy/tenant_b.json         |
| tenant_c  | user011_t3              | data/multitenancy/tenant_c.json         |
| tenant_d  | user016_t4              | data/multitenancy/tenant_d.json         |
| tenant_e  | user021_t5              | data/multitenancy/tenant_e.json         |

Note: this is a simple multi-tenancy setup example

## How to create custom data
Example for the `legal-entities-with-users.json` (other files are: `serviceagreements.json` and `products.json`):

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

Note: it is also possible to name/place the json file differently with the specific properties: `legal.entities.with.users.json.location` and `serviceagreements.json.location`
