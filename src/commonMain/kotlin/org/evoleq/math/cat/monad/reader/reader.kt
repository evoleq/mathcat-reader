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
package org.evoleq.math.cat.monad.reader

import org.evoleq.math.cat.functor.Diagonal
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.morphism.Morphism
import org.evoleq.math.cat.morphism.by
import org.evoleq.math.cat.morphism.evaluate
import org.evoleq.math.cat.morphism.o
import org.evoleq.math.cat.structure.x

interface Reader<E, T> : Morphism<E, T> {
    @MathCatDsl
    infix fun runOn(e : E): T = by(this@Reader)(e)
}

/**
 * Constructor function for [Reader]
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, T> Reader(arrow: (E)->T): Reader<E, T> = object : Reader<E, T> {
    override val morphism: (E) -> T = arrow
}

/**********************************************************************************************************************
 *
 * Functorial structure
 *
 **********************************************************************************************************************/
/**
 * Map [Reader]
 */
infix  fun <E, S, T>  Reader<E, S>.map(f: (S)->T): Reader<E, T> = Reader { e ->
    (f o by(this@map)) (e)
}
/**********************************************************************************************************************
 *
 * Applicative structure
 *
 **********************************************************************************************************************/
/**
 * Apply function of the Applicative [Reader]
 */
fun <E, S, T> Reader<E, (S)->T>.apply(): (Reader<E, S>)->Reader<E, T> = {rS -> Reader{e ->
    ((by(this@apply) x by(rS)) (Diagonal(e))).evaluate()
} }
/**
 * Apply function of the Applicative [Reader]
 */
infix fun <E, S, T> Reader<E, (S)->T>.apply(reader: Reader<E, S>): Reader<E, T> = apply()(reader)

/**********************************************************************************************************************
 *
 * Monadic structure
 *
 **********************************************************************************************************************/
/**
 * Unit of the [Reader] monad
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, T> ReturnReader(t : T): Reader<E, T> = Reader { t }

/**
 * Multiplication of the [Reader] mondad
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, T> Reader<E, Reader<E, T>>.multiply(): Reader<E, T> = Reader { e ->
    this@multiply runOn e runOn e
}

/**
 * Bind function of the [Reader] monad
 */
@MathCatDsl
infix fun <E, S, T> Reader<E, S>.bind(f: (S)-> Reader<E, T>): Reader<E, T> = (this map f).multiply()

/**
 * Kleisli [Reader]
 */
interface KlReader<E, K, T> : Morphism<K, Reader<E, T>>

/**
 * Constructor function for [KlReader]
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, K, T> KlReader(arrow: (K)-> Reader<E, T>): KlReader<E, K, T> = object : KlReader<E, K, T> {
    override val morphism = arrow
}

/**
 * Identity on the [KlReader] monoid
 */
@MathCatDsl
@Suppress("FunctionName")
fun <E, T> KlReturnReader(): KlReader<E, T, T> = KlReader { t -> ReturnReader(t) }

/**
 * Multiplication of [KlReader] arrows
 */
operator fun <E, K, L, M> KlReader<E, K, L>.times(other: KlReader<E, L, M>): KlReader<E, K, M> = KlReader { k ->
    (by(this@times)(k) map by(other)).multiply()
}
