# Data Hub Starter Project

Software
* DHS on AWS 2.9.8.1
* Data Hub: 5.1.0
* MarkLogic: 9.0-11
* Gradle: 5.2.1
* Java: 11.0.3

## Quickstart

### Setup your local MarkLogic server and data hub
Edit `docket-compose.yml` and set the MarkLogic admin username and password you want to use.

Create the container and start MarkLogic

```docker-compose up```

Deploy the data hub

```./gradlew mlDeploy```

Load the sample data

```./gradlew loadMoviesJson```

```./gradlew loadMoviesXml```

### Setup your DHS instance
Download your `gradle-dhs.properties` file from the DHS portal and edit to set the username and password for the user(s) you will be using

```
mlUsername=[replace_with_flow_developer_user]
mlPassword=[replace_with_flow_developer_password]
mlManageUsername=[replace_with_flow_developer_user]
mlManagePassword=[replace_with_flow_developer_password]
mlFlowOperatorUserName=[replace_with_flow_operator_user]
mlFlowOperatorUserPassword=[replace_with_flow_operator_password]
```

_Note: be sure `gradle-dhs.properties` is not checked in to a public source control repository as it will contain user names and passwords._

Load the sample data

```./gradlew -PenvironmentName=dhs loadMoviesJson```

```./gradlew -PenvironmentName=dhs loadMoviesXml```