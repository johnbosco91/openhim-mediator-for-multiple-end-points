# openhim-mediator-for-multiple-end-points
Java-based mediator for OpenHIM that handles the dynamic routing based on the payload ID/Code


Open up `src/main/resources/mediator.properties` and supply your OpenHIM config details and save:

```
  mediator.name=mediator-for-multiple-end-points
  # you may need to change this to 0.0.0.0 if your mediator is on another server than HIM Core
  mediator.host=localhost
  mediator.port=4000
  mediator.timeout=60000

  core.host=localhost
  core.api.port=8080
  # update your user information if required
  core.api.user=root@openhim.org
  core.api.password=openhim-password
```

To build and launch our mediator, run

```
  mvn install
  java -jar target/hdr-mediator-emr-0.1.0-jar-with-dependencies.jar
```

