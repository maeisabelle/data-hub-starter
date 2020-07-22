# Data Hub Starter Project

## Software
* DHS on AWS 2.9.8.1
* Data Hub: 5.1.0
* MarkLogic: 9.0-11
* MarkLogic Java Client: 4.2.0
* Gradle: 5.2.1
* Java: 11.0.3

## Setup your local MarkLogic server and data hub
Edit `docker-compose.yml` and set the MarkLogic admin username and password you want to use.

### Create the container and start MarkLogic

```docker-compose up```

### Setup your local build properties
Create your `gradle-local.properties` file and set the username and password you set when you created the MarkLogic container. Add the following to `gradle-local.properties` as a starting point:

```
# Put your overrides from gradle.properties here
# Don't check this in to version control

mlUsername=<admin username>
mlPassword=<admin password>

sslFlag=false
```

### Deploy the data hub and data service code

```./gradlew mlDeploy```

### Load the sample data

```./gradlew loadMoviesJson```

```./gradlew loadMoviesXml```

### Run the data service client
```
./gradlew runMovieSearchService -PsearchString="2002"
```
or
```
curl --digest -u <user>:<password>  http://localhost:8011/ds/movies/movieSearch.sjs?searchString=2002
```

## Setup your DHS properties and deploy
Once you have created your MarkLogic Data Hub Service via [MarkLogic Cloud Services](https://docs.marklogic.com/cloudservices/), download your `gradle-dhs.properties` file from the DHS portal and edit it to set the username and password for the user(s) you created for the service an will use for development and deployment:

```
mlUsername=[replace_with_flow_developer_user]
mlPassword=[replace_with_flow_developer_password]
mlManageUsername=[replace_with_flow_developer_user]
mlManagePassword=[replace_with_flow_developer_password]
mlFlowOperatorUserName=[replace_with_flow_operator_user]
mlFlowOperatorUserPassword=[replace_with_flow_operator_password]
```

_Note: be sure `gradle-dhs.properties` is not checked in to a public source control repository as it will contain user names and passwords. If you would like to check your `gradle-dhs.properties` into source code control, you can leave out any sensitive properties and speficy them as command line parameters using `-PparameterName=parameterValue` arguments to gradle._

### Deploy the data service code
```./gradlew hubDeploy -PenvironmentName=azure -i```
```./gradlew dhsDeploy -PenvironmentName=aws -PmlDHFVersion=5.1.0 -i```

### Load the sample data

```./gradlew -PenvironmentName=<env> loadMoviesJson```

```./gradlew -PenvironmentName=<env> loadMoviesXml```

### Run the data service client
```
./gradlew runMovieSearchService -PsearchString="2002" -PenvironmentName=<env>
```
or
```
curl -u <user>:<password>  https://<DHS hostname>:8011/ds/movies/movieSearch.sjs?searchString=2002
```

# Unit Testing
This project also includes configuration required to write and run unit tests using the [MarkLogic Unit Test](https://marklogic-community.github.io/marklogic-unit-test/) framework.

The default setup of this project deploys the unit testing framework and tests along with the application code. 

If you don't want the test code to be deployed to a specific environment, override the `mlModulePaths` property in your environment-specific `gradle-<env>.properties` file. Set it to the following to only deploy from `src/main/ml-modules`:

```
mlModulePaths=src/main/ml-modules
```

## Run via the framework directly
To run the unit tests via the framework's browser interface, connect to the app server for the database you want to run the tests in.

To run in the __data-hub-STAGING__ database:

```
http://<host>:8010/test/default.xqy
```

To run in the __data-hub-FINAL__ database:

```
http://<host>:8011/test/default.xqy
```

See https://marklogic-community.github.io/marklogic-unit-test/running/ for details on running the tests via gradle or the REST interface.

## Run via JUnit
The unit tests can also be run via JUnit which has been configured for the project. The `RunDataHubUnitTestsTest` class provides the hooks for JUnit to run all of the tests in the _marklogic-unit-test_ framework.

Be sure to set these properties in your `gradle-<env>.properties` file that you will use when running the tests:

```
mlTestUsername=<user to run tests as>
mlTestPassword=<password>
mlTestDbName=data-hub-STAGING|data-hub-FINAL
mlTestPort=8010|8011
```

Run the tests

```
./gradlew cleanTest test
```

The test reports are stored in the `build/reports/tests/test` directory. Open `index.html` to view the results of the last test run.

# References
1. Data Hub 5.1 gradle tasks - https://docs.marklogic.com/datahub/5.1/tools/gradle/gradle-tasks.html
2. Data Hub: 5.1.0 - https://docs.marklogic.com/datahub/5.1/index.html
3. MarkLogic: 9.0-11 - https://developer.marklogic.com/products/marklogic-server/9.0
