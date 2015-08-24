Workflow Module
===============

The workflow module is a bounded context covering workflow models, process definitions and process instances (executions).
It is contained in the package `ucles.weblab.common.worfklow`. It wraps the behaviour of a third party workflow engine,
in this case Activiti. By providing this _Façade_, the underlying workflow engine can be swapped out
without any change to other application code.

Plentiful and high quality documentation on Activiti is available at in its user guide at <http://activiti.org/userguide/index.html>.

Core Concepts
-------------

A _Process Model_ is a persistent, editable workflow process which cannot be instantiated. A graphical editor is provided
for manipulating models.

A _Process Definition_ is a deployed workflow Process Model, which cannot be edited but can be instantiated.

A _Process Instance_ is an executable instance of a Process Definition.

A _Step_ is an individual activity executed in the context of a Process Instance.

A _Task_ is a Step which is performed by a human being.

Web API
-------
The Web API is implemented in `webapi.WorkflowController` and is based at URI `/api/workflow`. The following entry points
are defined:

 URI                                            | Method    | Response Type         | Description
------------------------------------------------|-----------|-----------------------|------------------------------------------------------------------------------------------------------------------
 /api/workflow/processes/                       | GET       | Process Definition    | Fetches a list of the current versions of all process definitions.
 /api/workflow/processes/_id_/                  | GET       | Process Definition    | Fetches a single process definition.
 /api/workflow/processes/_id_/                  | GET       | application/xml       | Fetches a single process definition in BPMN 2.0 XML format.
 /api/workflow/processes/_id_/                  | GET       | image/png             | Fetches a single process definition as a diagram.
 /api/workflow/processes/_id_/                  | PUT       | Process Definition    | Deploys a new process definition version from a linked model.
 /api/workflow/processes/_id_/model/            | GET       | -                     | Redirects to a newly-created model for the deployed process.
 /api/workflow/models/_id_                      | GET       | Process Model         | Fetches process model metadata.
 /api/workflow/models/_id_                      | GET       | application/xml       | Fetches process model in BPMN 2.0 XML format.
 /api/workflow/models/_id_                      | GET       | image/png             | Fetches process model as a diagram.
 /api/workflow/instanceKey/_key_/tasks/_id_/    | POST      | -                     | Completes a user task with POSTed input data (application/x-www-form-urlencoded).
 /api/workflow/instanceKey/_key_history/steps/  | GET       | Audit Trail           | Returns a complete audit trail for a process instance.

All bodies are in JSON format (even for exceptions) except where indicated. Where multiple response types are possible for the same URI
then content negotiation or registered file extensions can be used to determine the correct response to give e.g.

    GET /api/workflow/models/_id_.xml

will return the BPMN 2.0 XML response even if the current `Accept` header would allow a JSON response (the default).

The following non-2xx status codes are also returned:

* _204 No Content_ - in response to a POST request to complete a user task.
* _303 See Other_ - in response to a GET request to create a model from a process definition.
* _400 Bad Request_ - if the `describedby` link on a PUT request to update a process definition does not reference a valid model.
* _404 Not Found_ - if an _id_ is specified which does not match known records.
* _500 Internal Server Error_ - for any unhandled exception.
* Plus all standard status codes for exceptions declared in `org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler`.

### Process Definition
A process definition is defined in `webapi.resource.WorkflowProcessDefResource` as follows:

```json
    {
      "key": "cieFulfilmentProcess",                // Unique key common to all versions of a process definition
      "name": "Fulfilment Process",                 // Display name of the process definition
      "version": 1,                                 // Version number of revision
      "deployInstant": "2015-08-04T23:54:06.123Z",  // UTC ISO date when this version was deployed
    }
```
#### Links
The following HATEOAS links are provided on a process definition:

* _self_ - URI of the process definition
* _canonical_ - URI of the process definition with `.xml` file extension, to force BPMN 2.0 XML representation
* _alternate_ - URI of the process definition with `.png` file extension, to force PNG diagram representation
* _describedby_ - URI to either create a new or return an existing editable model for the deployed process definition

By PUTting back to _self_ with the _describedby_ link set appropriately, a new process definiton version may be deployed.

### Process Model
A process model is defined in `webapi.resource.WorkflowModelResource` as follows:

```json
    {
      "key": "cieFulfilmentProcess",                    // Unique key matching the process definition
      "name": "Fulfilment Process",                     // Display name of the model
      "lastUpdateInstant": "2015-08-05T16:25:24.197Z",  // UTC ISO date when this model was last updated
    }
```
#### Links
The following HATEOAS links are provided on a process model:

* _edit-form_ - relative URI of a graphical editor for the process model
* _canonical_ - URI of self with `.xml` file extension, to force BPMN 2.0 XML representation

The model is edited using the modeler at the `edit-form` link which, unlike the others, is relative e.g. `/modeler.html?modelId=2501`.
See below for more details.

### Audit Trail
An audit trail record is defined in `webapi.resource.WorkflowAuditResource` as follows:

```json
    {
      "actor": "bodeng",                            // Which user performed the step (if any)
      "action": "Request Payment",                  // The name of the step (steps with no names are omitted from the audit trail)
      "auditInstant": "2015-08-05T19:31:22.103Z",   // The UTC ISO date when the step was completed
      "duration": "PT14M24S"                        // The UTC ISO duration between start and completion of the step
    }
```
For system steps, no `actor` will be listed and the duration will typically be very short. For user tasks, the duration
reflects the period of time the task was available to the user to perform before they completed it by submitting data to
the Web API described above.

Activiti Modeler and REST API Integration
-----------------------------------------

Activiti Modeler is part of the core Activiti distribution and is merged into this web application at build time by
tasks in the `pom.xml` which extract it, and the necessary REST servlets to support it, from the `activiti-webapp-explorer2`
artifact. This merge happens before the main resources phase for the build, so local resources can override those shipped
with Activiti. We use this to override `editor-app/app-cfg.js` with a version which has an empty context root specified.

The servlets extracted from Activiti Explorer to support the Modeler offer the following non-RESTful API which is only consumed by the Modeler.

 URI                | Method    | Response Type         | Description
--------------------|-----------|-----------------------|------------------------------------------------------------------------------------------------------------------
 /editor/stencilset | GET       | application/json      | Used to populate UI
 /model/_id_/json   | GET       | application/json      | Retrieves a single process model in a JSON form for the Modeler.
 /model/_id_/save   | PUT       | -                     | Updates a process model, including the BPMN 2.0 XML source and PNG generated from SVG content.

The first two APIs are used when the Modeler starts up and the last one is used every time the user hits save.

Closing the Modeler always redirects to the root of the application.

Process Instantiation
---------------------

The Service API for the Workflow module is defined in `domain.WorkflowService`. One method is of particular interest since
it is the way that process instances are created. It will be noted that whilst process definitions are created through
auto-deploy at startup, and process models are created from process definitions, there is no mechanism in the Web API
for creating process instances.

One way that process instances may be created is by firing a message event into the workflow engine. The `domain.WorkflowService`
API defines one method for despatching message events into the workflow engine:
```java
    boolean handleEvent(String eventName, String businessKey)
```
This method will check for an existing process instance associated with the business key. If there is one, it will
fire the message event at that process. If that process has any message-driven tasks then they will execute accordingly.
If there is no existing process instance, then the method attempts to find a process definition with a start event which
is message-driven and start a new instance for that definition.

The return value of this method indicates if the event caused either of these actions.

Database
--------

The domain is implemented in interfaces in the `domain` package. A JPA implementation is provided in the `domain.jpa`
package. DDD concepts are used i.e:

* _Aggregate_ - cluster of domain objects that can be treated as a single unit. In this case `WorkflowTaskAggregate` which includes `WorkflowTaskFormField` objects.
* _Value object_ - unadorned, unidentified data structure (e.g. the domain object `HistoricWorkflowStep`).
* _Entity_ - persistable domain object. Distinct from the above in that instances are uniquely identifiable other than by their data (i.e they have an ID).
* _Factory_ - not required here as all entities are created by the underlying workflow engine
* _Repository_ - place where entities can be retrieved and updated e.g. `WorkflowTaskRepository`.
* _Service_ - operations on domain objects which do not fit in the repository

Some of the interfaces also have a nested `Builder` interface which can be used with `ucles.weblab.common.domain.BuilderProxyFactory`
to obtain instances of the interface implemented by a proxy. The builders are all exposed through `java.util.function.Supplier`
objects as beans.

None of the repositories or entities in this module are backed directly by a database table but are instead implemented
as facades and adapters over the corresponding Activiti services and entities i.e.

 Entity                     | Backing entity                                        | Backing service(s)
----------------------------|-------------------------------------------------------|---------------------------------
 DeployedWorkflowProcess    | org.activiti.engine.repository.ProcessDefinition      | org.activiti.engine.RepositoryService
 EditableWorkflowProcess    | org.activiti.engine.repository.Model                  | org.activiti.engine.RepositoryService
 HistoricWorkflowStep       | org.activiti.engine.history.HistoricActivityInstance  | org.activiti.engine.HistoryService
 WorkflowTaskAggregate      | org.activiti.engine.task.Task / org.activiti.engine.form.FormProperty | org.activiti.engine.TaskService / org.activiti.engine.FormService

Activiti itself uses the database to hold model (`act_re_model`), process definition (`act_re_procdef`), instance (`act_ru_execution`)
and audit (`act_hi_taskinst`) data. Refer to the Activiti documentation for further details.

Spring Boot Integration
-----------------------

### Auto-deployment
Activiti's Spring Boot integration will automatically deploy processes on the classpath under `/processes` at runtime into the
workflow engine.

### Access to entities
Any JPA entities loaded into process variables during a proces execution will merge and refresh their state correctly
with the database correctly. However, this relies on them having simple primary keys which can be converted to a String.
Entities with composite primary keys should not be held in process variables as Activiti will throw an error when trying
to refresh them at the next workflow step (as it only really holds the primary key between steps).

### Access to beans
Any Spring beans can be called directly from workflow service tasks using Spring EL expressions. This allows us to use
the domain repositories and services to manipulate the model directly from the workflow without implementing custom tasks.

### Spring Security
By default, Activiti configures Spring Security to delegate authentication to its own `IdentityService`. An application wanting
to implement some other authentication mechanism (e.g. Active Directory) should exclude this auto-configuration using:
```java
@EnableAutoConfiguration(exclude={org.activiti.spring.boot.SecurityAutoConfiguration.class})
```

User Task Forms
---------------

Activiti allows forms consisting of a set of form field definitions to be embedded within user task definitions in a model
or process. These form definitions are returned as part of a `WorkflowTaskAggregate` and can be used by a UI to present
input fields directly to the end user for completion. The Web API to complete a user task will then accept these fields
POSTed to it and complete the task.
