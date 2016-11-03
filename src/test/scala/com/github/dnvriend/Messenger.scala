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

package com.github.dnvriend

import akka.persistence.PersistentActor

class Messenger(val persistenceId: String, override val journalPluginId: String = "") extends PersistentActor {
  var events: Seq[Any] = Seq.empty[Any]
  override def receiveRecover: Receive = {
    case event: MyMessage => events :+= event
    case event            =>
  }

  override def receiveCommand: Receive = {
    case "state" =>
      sender() ! events
    case str: String => persist(MyMessage(str)) { _ =>
      sender() ! "ack"
    }
  }
}
