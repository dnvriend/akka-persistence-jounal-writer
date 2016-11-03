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

package akka.persistence.journal

import akka.actor.{ ActorContext, ActorRef }
import akka.persistence.JournalProtocol.WriteMessages
import akka.persistence.query.{ EventEnvelope, EventEnvelope2 }
import akka.persistence.{ AtomicWrite, PersistentRepr }

import scala.collection.immutable.{ Map, Seq }
import scala.reflect.ClassTag

package object writer {
  val unsupported: Throwable = new IllegalArgumentException("JournalWriter only accepts EventEnvelope, immutable.Seq[EventEnvelope], EventEnvelope2 and immutable.Seq[EventEnvelope2]")

  def replyWithFailure(replyTo: ActorRef, cause: Throwable = unsupported)(implicit ctx: ActorContext, self: ActorRef): Unit = {
    replyTo ! akka.actor.Status.Failure(cause)
    ctx.stop(self)
  }

  def replyWithSuccess(replyTo: ActorRef, msg: String = "ack")(implicit ctx: ActorContext, self: ActorRef): Unit = {
    replyTo ! msg
    ctx.stop(self)
  }

  implicit class SeqOps(val that: Seq[_]) extends AnyVal {
    def is[A](implicit ct: ClassTag[A]): Boolean =
      that.nonEmpty && that.forall(_.getClass == ct.runtimeClass)
    def as[A: ClassTag]: Seq[A] = that.map(_.asInstanceOf[A])
  }

  def toPersistentRepr(env: EventEnvelope): PersistentRepr =
    PersistentRepr(
      payload = env.event,
      sequenceNr = env.sequenceNr,
      persistenceId = env.persistenceId
    )

  def listOfEnvelopeToRepr(xs: Seq[EventEnvelope]): Seq[PersistentRepr] =
    xs.map(toPersistentRepr)

  def listOfReprToAtomicWrite(xs: Seq[PersistentRepr]): AtomicWrite =
    AtomicWrite(xs)

  def toAtomicWrite(xs: Seq[EventEnvelope]): Seq[AtomicWrite] = {
    val groupedByPid: Map[String, AtomicWrite] =
      xs.groupBy(_.persistenceId)
        .mapValues(listOfEnvelopeToRepr)
        .mapValues(listOfReprToAtomicWrite)
    Seq(groupedByPid.values.toList: _*)
  }

  def toEventEnvelope(x: EventEnvelope2): EventEnvelope =
    EventEnvelope(0L, x.persistenceId, x.sequenceNr, x.event)

  def writeMessages(xs: Seq[EventEnvelope], writerJournalAdapter: ActorRef): WriteMessages =
    WriteMessages(toAtomicWrite(xs), writerJournalAdapter, 1)
}
