# akka-persistence-journal-writer

[![Build Status](https://travis-ci.org/dnvriend/akka-persistence-jounal-writer.svg?branch=master)](https://travis-ci.org/dnvriend/akka-persistence-jounal-writer)
[ ![Download](https://api.bintray.com/packages/dnvriend/maven/akka-persistence-journal-writer/images/download.svg) ](https://bintray.com/dnvriend/maven/akka-persistence-journal-writer/_latestVersion)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

__akka-persistence-query-writer__ consists of an akka-streams `Flow` and `Sink` that makes it possible to write
`EventEnvelope` , `Seq[EventEnvelope]`, `EventEnvelope2` or `Seq[EventEnvelope2]` to __any__ akka-persistence jounal.
It does this by sending messages directly to the [journal plugin itself](http://doc.akka.io/api/akka/2.4/#akka.persistence.journal.japi.AsyncWriteJournal).

## Installation
Add the following to your `build.sbt`:

```scala
// the library is available in Bintray's JCenter
resolvers += Resolver.jcenterRepo

libraryDependencies += "com.github.dnvriend" %% "akka-persistence-journal-writer" % "0.0.2"
```

## Contribution policy
Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

## Basic Use Case
The `akka-persistence-journal-writer` lets you write events to any journal. It accepts only two types of messages:

- [akka.persistence.query.EventEnvelope](http://doc.akka.io/api/akka/2.4/#akka.persistence.query.EventEnvelope)
- [akka.persistence.query.EventEnvelope2](http://doc.akka.io/api/akka/2.4/#akka.persistence.query.EventEnvelope2)

Of course, you can send `immutable.Seq[EventEnvelope]` or `immutable.Seq[EventEnvelope2]` of those too for bulk loading.

The basic use case would be loading one event store into another. In this example we will be loading events from
the inmemory-journal, using akka-persistence-query and loading the events into the level-db journal:

```scala
import akka.stream.scaladsl._
import akka.persistence.query._
import akka.persistence.query.scaladsl._

val inMemoryReadJournal = PersistenceQuery(system).readJournalFor("inmemory-read-journal")
 .asInstanceOf[ReadJournal with CurrentPersistenceIdsQuery with CurrentEventsByPersistenceIdQuery]

val result: Future[Done] =
 inMemoryReadJournal.currentPersistenceIds().flatMapConcat { pid =>
  inMemoryReadJournal.currentEventsByPersistenceId(pid, 0, Long.MaxValue)
 }.grouped(100).runWith(JournalWriter.sink("akka.persistence.journal.leveldb"))
```

The fragment above reads all events from all persistenceIds from the `inmemory-journal` using akka-persistence-query
and writes them directly into an __empty__ level-db journal. Of course, any journal will work.

## Converting serialization strategy
Some akka-persistence-query compatible plugins support using the `event-adapters` from the `write-plugin`, therefor converting
the journal's data-model to the application-model when querying the journal. Say for example that you have a journal that
uses the Java-serialization strategy to store events and you would like to convert all those event using another serialization
strategy, say `Protobuf`, then you can just configure some event adapters on the other write-plugin, say the level-db plugin and
just load events from the in-memory plugin and store them into the level-db plugin. Akka-persistence will do all the work
for you because you have configured the event-adapters on the plugin to do the serialization.

The solution is the configuration of the event-adapters on the write-plugins like eg:

```bash
inmemory-journal {
  event-adapters {
    adapter-a = "com.github.dnvriend.EventAdapterA"
  }
  event-adapter-bindings {
    "com.github.dnvriend.MyMessage" = adapter-a
  }
}

inmemory-read-journal {
  write-plugin = "inmemory-journal"
}

akka.persistence.journal.leveldb {
  dir = "target/journal"
  event-adapters {
    adapter-b = "com.github.dnvriend.EventAdapterB"
  }
  event-adapter-bindings {
    "com.github.dnvriend.MyMessage" = adapter-b
  }
}

akka.persistence.query.journal.leveldb {
  write-plugin = "akka.persistence.journal.leveldb"
}
```

In the example above, all events will be written to the in-memory journal using the event-adapter `AdapterA`.
The inmemory-read-journal has been configured to use the event-adapters as configured from the inmemory-journal
so when reading events using akka-persistence-query it should return application-domain events and not the java-serialized byte arrays.

When asking the akka-persistence-journal-writer (JournalWriter) to write events to a write plugin with a certain journalPluginId,
eg. the level-db plugin, that plugin has been configured with certain event-adapters. Imagine that those event adapters will
convert application-domain events to Protobuf types, then the protobuf serializer will serialize events to byte arrays and store
those events in the level-db journal.

Of course, all events in the inmemory-journal will stay untouched. All events in the level-db journal will be protobuf-encoded.

## Bulk loading events
Say for a moment, that you have some business level entity with a stable identifier, and you have a lot of those. But those entities
do not yet exist. They will exist only when they are created in the journal; ie. they have an initial state and a persistenceId of course.

Also imagine that the initial state is in some very large CSV file or JSON file that has been provided to you by an Apache Spark job for example.

You _could_ do the following:

- read each record,
- convert to an event,
- send events to shard,
- persistent actor will be recovered,
- event will be stored,
- persistent actor will be passified,
- actor will be unloaded from memory.

This is in the book...

You could also do the following:

- read each record with a [FileIO](http://doc.akka.io/api/akka/2.4/#akka.stream.scaladsl.FileIO$) source
- convert the CSV to a the known event, wrap into a Seq[EventEnvelope]
- directly store these events in the journal

This will have no persistent-actor life cycle overhead and will be much faster.

Its only applicable in some use cases of course.

# What's new?
## 0.0.2 (2016-11-03)
  - cross scala 2.11.8 and 2.12.0 build against akka 2.4.12

## 0.0.1 (2016-11-03)
  - First release


