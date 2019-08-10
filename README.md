## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will pay those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

### Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── pleo-antaeus-app
|
|       Packages containing the main() application. 
|       This is where all the dependencies are instantiated.
|
├── pleo-antaeus-core
|
|       This is where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|
|       Module interfacing with the database. Contains the models, mappings and access layer.
|
├── pleo-antaeus-models
|
|       Definition of models used throughout the application.
|
├── pleo-antaeus-rest
|
|        Entry point for REST API. This is where the routes are defined.
└──
```

## Instructions
Fork this repo with your solution. We want to see your progression through commits (don’t commit the entire solution in 1 step) and don't forget to create a README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

Happy hacking 😁!

## How to run
```
./docker-start.sh
```

## Libraries currently in use
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library

## Implementation notes

### Considerations

#### How to run billing cycle?

We have 2 options:
- API call and have external scheduler:
  - Pros:
     - Simpler implementation.
     - Can defer scheduling to external system (separate service, cron job etc.).
  - Cons:
     - If we need to make any decision based on the result of processing data, we need put the logic into an external scheduler and pass enough information for making a decision as a result of API call (maybe not a cons at all).
- In-process scheduler:
  - Pros - can implement business logic of scheduling as part of the same codebase.
  - Cons - harder to implement, can't change scheduler without restarting the service. Usually it's not the best idea to combine long-running job and API calls in the same process.

First implementation will be an API call.

#### How to implement PaymentProvider?

I see we have NetworkException and I looked around in Pleo tasks and found Tinjis project.
Looks like in real application there is external system for performing actual payments.

Ways to implement payments:
- Directly call external API.
- Put payments to process into a queue and have separate service.

To use queues we would need to refactor solution: instead of putting invoices into the database and scheduling payments we
would put them into a queue immediately and then in a separate service process the queue and manage scheduling, failed payments etc.
While I believe using a queue for communication has it's benefits, it will increase complexity of the solution and
I consider it to be outside of the scope.

Then we have to think about:
- Testing - probably there should be mock implementation of PaymentProvider to test BillingService,
  but how to test PaymentProvider itself? One option is to rely on external mock service like in Tinjis project.
- Local development - how to run the service in local environment for smoke test? Tinjis project again?
- Configuration for different environments - provide endpoint as parameter and read it from environment variable
  like in 12-factor apps (and like in Tinjis project).

#### How to implement BillingService?

Questions:
- API call failure handling - what happens when we process half of payments and get an error?
  We can mark as Done only invoices that were successfully processed, but what about scheduler?
- Do we need to reschedule payments earlier than in the next month (probably yes) if there were errors?
  One option is to return True from BillingService when there is at least one pending invoice.
- Since requirements say _"schedule payment of those invoices on the first of the month"_,
  I assume we need to process only payments for the previous month(s), thus, if we want to reprocess
  invoices on 2nd or 15th of a month (due to any failure), we still need to process only invoices from previous month(s).

  I don't want to assume that we have synchronized clocks between scheduler and API, thus prefer to pass
  the billing timestamp as parameter to the BillingService, it means the service will process only invoices with the date
  before the provided timestamp. This allows fine grained control of billing cycle without code changes.

  I assume we want to reprocess all old invoices even before the billing month, thus no start date.

### Questions outside of scope of the task

These parts are missing from the implementation and might be necessary depending on requirements:
- Authentication and authorisation
- Metrics
