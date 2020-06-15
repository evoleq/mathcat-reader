package org.evoleq.math.cat.suspend.monad.reader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import org.evoleq.math.cat.marker.MathCatDsl
import org.evoleq.math.cat.suspend.morphism.ScopedSuspended
import org.evoleq.math.cat.suspend.morphism.by

interface Reader<E, T> : ScopedSuspended<E, T> {
    suspend infix  fun <S>  map(f: suspend CoroutineScope.(T)->S): Reader<E, S> = Reader { e ->
        f(by(this@Reader)(e))
    }
    @MathCatDsl
    suspend infix fun runOn(e : E): T = coroutineScope { by(this@Reader)(e) }
    
    @MathCatDsl
    suspend infix fun <S> bind(f: suspend CoroutineScope.(T)-> Reader<E, S>): Reader<E, S> = (this map f).multiply()
}

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> Reader(arrow: suspend CoroutineScope.(E)->T): Reader<E, T> = object : Reader<E, T> {
    override val morphism: suspend CoroutineScope.(E) -> T = arrow
}

interface KlReader<E, K, T> : ScopedSuspended<K, Reader<E, T>>

@MathCatDsl
@Suppress("FunctionName")
fun <E, K, T> KlReader(arrow: suspend CoroutineScope.(K)-> Reader<E, T>): KlReader<E, K, T> = object : KlReader<E, K, T> {
    override val morphism: suspend CoroutineScope.(K) -> Reader<E, T> = arrow
}

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> ReturnReader(t : T): Reader<E, T> = Reader { t }

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> KlReturnReader(): KlReader<E, T, T> = KlReader { t -> ReturnReader(t) }

@MathCatDsl
@Suppress("FunctionName")
suspend fun <E, T> Reader<E, Reader<E, T>>.multiply(): Reader<E, T> = Reader { e ->
    this@multiply runOn e runOn e
}

suspend operator fun <E, K, L, M> KlReader<E, K, L>.times(other: KlReader<E, L, M>): KlReader<E, K, M> = KlReader { k ->
    (by(this@times)(k) map by(other)).multiply()
}
