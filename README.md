# user-task-management

##### The main purpose of the user task management service is to provide REST API low-code platform:

* `The application brings the following functionality:`
    * access to user task lists based on roles and permissions;
    * performing tasks assigned to the user;

##### Spring Actuator configured with Micrometer extension for exporting data in prometheus-compatible format.

*End-point:* <service>:<port>/actuator/prometheus

*Prometheus configuration example (prometheus.yml):*

```
global:
  scrape_interval: 10s
scrape_configs:
  - job_name: 'spring_micrometer'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets: ['< service >:< port >']
```

##### Spring Sleuth configured for Istio http headers propagation:

- x-access-token
- x-request-id
- x-b3-traceid
- x-b3-spanid
- x-b3-parentspanid
- x-b3-sampled
- x-b3-flags
- b3

##### Running the tests:

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### Local development

1. `application-local.yml` is configuration file for local development;
2. to interact with `digital signature ops` service, set `dso.url` variable as an environment
   variable or specify it in the configuration file;
3. to interact with `business process management` service, set `bpms.url` variable as environment
   variable or specify it in the configuration file: 
   * by default http://localhost:8080;
4. logging settings (*level,pattern,output file*) specified in the configuration file;
5. ceph settings (*http-endpoint,access-key,secret-key,bucket*) specified in the configuration file;
6. run spring boot application using 'local' profile:
    * `mvn spring-boot:run -Drun.profiles=local` OR using appropriate functions of your IDE;
7. the application will be available on: http://localhost:8888/user-task-management/swagger

##### Logging:

* `Default:`
    * For classes with annotation RestController/Service, logging is enabled by default for all
      public methods of a class;
* `To set up logging:`
    * *@Logging* - can annotate a class or method to enable logging;
    * *@Confidential* - can annotate method or method parameters to exclude confidential data from
      logs:
        - For a method - exclude the result of execution;
        - For method parameters - exclude method parameters;