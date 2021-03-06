/*
 * Copyright (C) 2013 Alcatel-Lucent.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * Licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package molecule
package channel

/**
 * A factory for output channels.
 */
class OChanFactory[-A](val mk: () => OChan[A]) extends Function0[OChan[A]] {

  /**
   * Create a new output channel
   *
   * @return an output channel
   */
  def apply(): OChan[A] = mk()

}

/**
 * Factory object for output channel factories.
 */
object OChanFactory {

  /**
   * Wrap a function that creates an output channel into a factory.
   *
   * @return an output channel factory
   */
  def apply[A](mk: () => OChan[A]): OChanFactory[A] = new OChanFactory(mk)

  implicit def ochanFactoryIsMessage[A]: Message[OChanFactory[A]] = PureMessage
}