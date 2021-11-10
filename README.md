# user-task-management

### Overview

* The main purpose of the user-task-management service is to provide REST API low-code platform to
  interact with user tasks;
* access to user task lists based on roles and permissions;
* performing tasks assigned to the user.

### Usage

#### Prerequisites:

* Ceph-storage is configured and running;
* business-process-management service is configured and running;
* digital-signature-ops service is configured and running;
* form-management-provider service is configured and running.

#### Configuration

Available properties are following:

* `bpms.url` - business process management service base url;
* `dso.url` - digital signature ops service base url;
* `form-management-provider.url` - form management service base url;
* `ceph.http-endpoint` - ceph base url;
* `ceph.access-key` - ceph access key;
* `ceph.secret-key` - ceph secret key;
* `ceph.bucket` - ceph bucket name.

#### Run application:

* `java -jar <file-name>.jar`

### Local development

1. Run spring boot application using 'local' profile:
    * `mvn spring-boot:run -Drun.profiles=local` OR using appropriate functions of your IDE;
    * `application-local.yml` - configuration file for local profile.
2. The application will be available on: http://localhost:8888/user-task-management/swagger.

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.
    
### License

The user-task-management is released under version 2.0 of
the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).