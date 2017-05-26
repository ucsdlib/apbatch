# ApBatch

ApBatch is a tool to manage electrical invoices, vendors and indexes for UCSD Library.  It is converted from FoxPro to a web based application.

## Requirements 

* JDK 1.7
* Ant
* PostgreSQL Database
* Tomcat

## Installation

```
$ git clone git@github.com:ucsdlib/apbatch.git
```

```
$ brew install postgresql
```

## Tomcat Configuration

1.Edit Tomcat conf/server.xml and add to the GlobalNamingResources:

```
$ xml
    <Environment name="jdbc/apbatch" value="jdbc/apbatch" type="java.lang.String"/>
    <Resource name="jdbc/apbatch" auth="Container" type="javax.sql.DataSource"
        username="ap_user" password="XXXX" driverClassName="org.postgresql.Driver"
        url="jdbc:postgresql://localhost/ap_user" maxActive="10" maxIdle="3"
        maxWait="5000" validationQuery="select 1" logAbandoned="true"
        removeAbandonedTimeout="60" removeAbandoned="true" testOnReturn="true"
        testOnBorrow="true"/>
```

## Usage

1.Open project.

```
$ cd apbatch
```

2.Checkout develop branch.

```
$ git checkout develop
```

3.Build apbatch.war

```
$ ant clean webapp
```

4.Deploy to Tomcat

```
$ ant local-deploy
```

## Running ApBatch

* Run Tomcat startup command:

```
$ On Linux: ./startup.sh
```

```
$ On Windows: ./startup.bat
```

## ApBatch will be available at 

```
http://localhost:3000/ 
```

## Running the JunitTest Suites

```
$ ant junitreport
```
