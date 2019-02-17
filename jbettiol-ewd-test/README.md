# jbettiol-ewd-demo

## Task Description:

We ask you to build, in JAVA language and framework of your choice, a Rest API that extends the Dropbox API functionality by introducing "tags".
Tags are lowercase, alphanumeric strings that can contain useful information about a file, such as "beach", "miami" for a photo, or "work", "cv", "application" for a job application document.API consumer should be able to:

### API consumer should be able to:

* Add any number of tags/remove them using the API.
* Search files by a specific tag or multiple tags (with AND or OR connection, preferably both). The search should preferably support pagination.
* The API should also allow consumer to download all files that have some specific tag(s) by zipping them together into a single file. The summed size of the files should be validated to be below 500MB (can be configurable externally as well) for this endpoint.

### Technical Requirements:

* The Dropbox API client app that your application will use should be externally configurable - means that we should be able to pass the needed keys, tokens, secrets to your app externally (i.e. via environment variables), and the application should make use of them, and not use built-in/hardcoded configuration.
* Tags should be indexed in an Apache SOLR instance. Its URL also should be configurable externally.
* A docker image should be built on app build phases, and we should be able to simply run a docker container from your image to test the app (instructions on how to run your app would be nice). (A docker-compose file which spawns everything up&ready to test would be extra nice)
* The API should follow RESTful API conventions & best practices.

## How to run

1) It only works in Eclipse!!  For some reason I have a ridiculous dependenct issue (only recently) via command line
2) The two test cases of relevance are TaggingApiTest and TaggingServiceTest
3) Be sure to update your application-test.properties file with a working token

Note: I am yet to test it with a remote server, just didn't get around to it!

## John's Preface / Excuses!!
* I haven't programmed any "real" work in Java since 2011.  Since that time I have done an Android app but it isn't anything to be proud of!  Hopefully you'll all forgive me for any screw-ups in the code!
* After building my own dropbox proxy (using RestTemplate) and associated unit tests (using MockMvc) I decided it was dumb and instead borrowed the library written by MajewskiKrzysztof, found here: https://github.com/MajewskiKrzysztof/spring-boot-dropbox  **Note:** Source code can be provide if necessary
* To aid with development pace I decided to use the SolrEmbeddedServer and remove it after I had all test cases working (if I didn't it means I ran out of time)
* I decided to use Springboot for this framework as it sounded interesting and as I am lazy Annotations seem like an easy way forward instead of configuration overload
* I originally created a Springboot project with Maven but I decided to move to Gradle to see how it works

## Components

1) DropboxService - Helper layer to simplify interactions with Dropbox (I did not write this!)
2) TaggingService - Abstraction layer to Solr server, handles all usecases mentioned in Task Description
3) DropboxIntTest - Unit tests to validate the dropbox layer (I didn't write this)
4) TaggingIntTest - Tagging Service integration tests


## Diary

I decided to start writing a diary as I was having so many stupid issues.

It is mainly due to me wanting to have good unit tests and using embedded solr for these tests

2019/02/12 0115

So, I have sent far more time trying to solve maven and gradle dependency issues more than anything else :(

This one has taken up many hours:

	at org.apache.logging.slf4j.Log4jLoggerFactory.getLogger(Log4jLoggerFactory.java:29)
	at org.slf4j.LoggerFactory.getLogger(LoggerFactory.java:358)
	at org.apache.logging.slf4j.SLF4JLoggerContext.getLogger(SLF4JLoggerContext.java:39)
	
It's a stackoverflow loop due to spring and solr having stupid dependency overlaps

Before this one I had an issue as I didn't realise my reposotories definition was in the wrong place and I was thus missing the restlet dependencies that are needed for solr :(

2019/02/12 0136 -- Holy crap.. I finally fixed all of the insane dependencies



