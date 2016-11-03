/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package akka.persistence.journal.writer

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.persistence.JournalProtocol._
import akka.persistence.Persistence
import akka.persistence.query.{ EventEnvelope, EventEnvelope2 }
import akka.stream.scaladsl._
import akka.util.Timeout
import akka.{ Done, NotUsed }

import scala.collection.immutable._
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

class WriteJournalAdapterCameo(writePlugin: ActorRef, replyTo: ActorRef, messages: Seq[EventEnvelope]) extends Actor {
  override def preStart(): Unit = {
    if (messages.isEmpty)
      replyWithSuccess(replyTo)
    else
      writePlugin ! writeMessages(messages, self)
  }

  override def receive: Receive = {
    case msg: WriteMessageSuccess =>
      replyWithSuccess(replyTo)
    case WriteMessagesSuccessful =>
      replyWithSuccess(replyTo)
    case WriteMessagesFailed(cause) =>
      replyWithFailure(replyTo, cause)
    case WriteMessageRejected(_, cause, _) =>
      replyWithFailure(replyTo, cause)
    case WriteMessageFailure(_, cause, _) =>
      replyWithFailure(replyTo, cause)
  }
}

class WriteJournalAdapter(writePlugin: ActorRef) extends Actor {
  def cameo(replyTo: ActorRef, writePlugin: ActorRef, messages: Seq[EventEnvelope]): ActorRef =
    context.actorOf(Props(new WriteJournalAdapterCameo(writePlugin, replyTo, messages)))

  override def receive: Receive = {
    case messages: Seq[_] if messages.isEmpty =>
      cameo(sender(), writePlugin, Seq.empty[EventEnvelope])
    case messages: Seq[_] if messages.is[EventEnvelope] =>
      cameo(sender(), writePlugin, messages.as[EventEnvelope])
    case messages: Seq[_] if messages.is[EventEnvelope2] =>
      cameo(sender(), writePlugin, messages.as[EventEnvelope2].map(toEventEnvelope))
    case msg: EventEnvelope =>
      cameo(sender(), writePlugin, Seq(msg))
    case msg: EventEnvelope2 =>
      cameo(sender(), writePlugin, Seq(toEventEnvelope(msg)))
    case _ =>
      replyWithFailure(sender(), unsupported)
  }
}

object JournalWriter {
  def flow[A](journalPluginId: String, parallelism: Int = 1)(implicit system: ActorSystem, ec: ExecutionContext, ct: ClassTag[A], timeout: Timeout = Timeout(1.minute)): Flow[A, A, NotUsed] = {
    assert(ct.runtimeClass == classOf[EventEnvelope] || ct.runtimeClass == classOf[EventEnvelope2] || ct.runtimeClass == classOf[Seq[EventEnvelope]] || ct.runtimeClass == classOf[Seq[EventEnvelope2]], s"element must be of type EventEnvelope, EventEnvelope2, immutable.Seq[EventEnvelope] or immutable.Seq[EventEnvelope2], type is: '${ct.runtimeClass}'")
    val journal: ActorRef = Persistence(system).journalFor(journalPluginId)
    val journalAdapter: ActorRef = system.actorOf(Props(new WriteJournalAdapter(journal)))
    Flow[A].mapAsync(parallelism)(element => (journalAdapter ? element).map(_ => element))
  }

  def sink[A: ClassTag](journalPluginId: String, parallelism: Int = 1)(implicit system: ActorSystem, ec: ExecutionContext, timeout: Timeout = Timeout(1.minute)): Sink[A, Future[Done]] =
    flow(journalPluginId, parallelism).toMat(Sink.ignore)(Keep.right)
}