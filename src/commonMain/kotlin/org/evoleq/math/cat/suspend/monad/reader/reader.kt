/**
 * Copyright (c) 2020 Dr. Florian Schmidt
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
package org.evoleq.math.cat.suspend.monad.reader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

interface Reader<E, T> : ScopedSuspended<E, T> {
    @MathCatDsl
    suspend infix fun runOn(e : E): T = coroutineScope { by(this@Reader)(e) }
}

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> Reader(arrow: suspend CoroutineScope.(E)->T): Reader<E, T> = object : Reader<E, T> {
    override val morphism: suspend CoroutineScope.(E) -> T = arrow
}
/**********************************************************************************************************************
 *
 * Functorial structure
 *
 **********************************************************************************************************************/
/**
 * Map [Reader]
 */
@MathCatDsl
suspend infix fun <E, S, T>  Reader<E, S>.map(f: suspend CoroutineScope.(S)->T): Reader<E, T> = Reader { e ->
    f(by(this@map)(e))
}


/**********************************************************************************************************************
 *
 * Applicative structure
 *
 **********************************************************************************************************************/
/**
 * Apply function of the applicative [Reader]
 */
@MathCatDsl
suspend fun <E, S, T> Reader<E, suspend CoroutineScope.(S)->T>.apply(): suspend CoroutineScope.(Reader<E, S>)->Reader<E, T> = {
    reader -> this@apply bind {f -> reader map f}
}

/**
 * Apply function of the applicative [Reader]
 */
@MathCatDsl
suspend infix fun <E, S, T> Reader<E, suspend CoroutineScope.(S)->T>.apply(reader: Reader<E, S>): Reader<E, T> = coroutineScope {
    apply()(reader)
}

/**********************************************************************************************************************
 *
 * Monadic structure
 *
 **********************************************************************************************************************/

/**
 * Return the [Reader] monad
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, T> ReturnReader(t : T): Reader<E, T> = Reader { t }

/**
 * Multiplication of the [Reader] monad
 */
@MathCatDsl
@Suppress("FunctionName")
suspend fun <E, T> Reader<E, Reader<E, T>>.multiply(): Reader<E, T> = Reader { e ->
    this@multiply runOn e runOn e
}

/**
 * Bind function of the [Reader]
 */
@MathCatDsl
suspend infix fun <E, S, T> Reader<E, S>.bind(f: suspend CoroutineScope.(S)-> Reader<E, T>): Reader<E, T> = (this map f).multiply()

/**
 * Kleisli [Reader]
 */
interface KlReader<E, K, T> : ScopedSuspended<K, Reader<E, T>>

/**
 * Constructor function for the [KlReader]
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, K, T> KlReader(arrow: suspend CoroutineScope.(K)-> Reader<E, T>): KlReader<E, K, T> = object : KlReader<E, K, T> {
    override val morphism: suspend CoroutineScope.(K) -> Reader<E, T> = arrow
}

/**
 * Identity element of the [Reader] monad
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, T> KlReturnReader(): KlReader<E, T, T> = KlReader { t -> ReturnReader(t) }

/**
 * Multiplication of [KlReader]s
 */
suspend operator fun <E, K, L, M> KlReader<E, K, L>.times(other: KlReader<E, L, M>): KlReader<E, K, M> = KlReader { k ->
    (by(this@times)(k) map by(other)).multiply()
}
