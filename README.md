# Data Hub Starter Project

## Software
* DHS on AWS 2.9.8.1
* Data Hub: 5.1.0 - https://docs.marklogic.com/datahub/5.1/index.html
* MarkLogic: 9.0-11 - https://developer.marklogic.com/products/marklogic-server/9.0
* MarkLogic Java Client: 4.2.0
* Gradle: 5.2.1
* Java: 11.0.3

## Quickstart

### Setup your local MarkLogic server and data hub
Edit `docket-compose.yml` and set the MarkLogic admin username and password you want to use.

#### Create the container and start MarkLogic

```docker-compose up```

#### Setup your local build properties
Create your `gradle-local.properties` file and set the username and password you set when you created the MarkLogic container. Add the following to `gradle-local.properties` as a starting point:
```
# Put your overrides from gradle.properties here
# Don't check this in to version control

mlUsername=<admin username>
mlPassword=<admin password>

sslFlag=false
```

#### Deploy the data hub and data service code

```./gradlew mlDeploy```

#### Load the sample data

```./gradlew loadMoviesJson```

```./gradlew loadMoviesXml```

#### Test the data service
```
./gradlew runMovieSearchService -PsearchString="2002"
```
or
```
curl --digest -u <user>:<password>  http://localhost:8011/ds/movies/movieSearch.sjs?searchString=2002
```

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

_Note: be sure `gradle-dhs.properties` is not checked in to a public source control repository as it will contain user names and passwords. If you would like to check your `gradle-dhs.properties` into source code control, you can leave out any sensitive properties and speficy them as command line parameters using `-PparameterName=parameterValue` arguments to gradle._

#### Deploy the data service code
```./gradlew dhsDeploy -PenvironmentName=dhs -i```

#### Load the sample data

```./gradlew -PenvironmentName=dhs loadMoviesJson```

```./gradlew -PenvironmentName=dhs loadMoviesXml```

#### Test the data service
```
./gradlew runMovieSearchService -PsearchString="2002" -PenvironmentName=dhs
```
or
```
curl -u <user>:<password>  https://<DHS hostname>:8011/ds/movies/movieSearch.sjs?searchString=2002
```

## References
Data Hub 5.1 gradle tasks - https://docs.marklogic.com/datahub/5.1/tools/gradle/gradle-tasks.html
