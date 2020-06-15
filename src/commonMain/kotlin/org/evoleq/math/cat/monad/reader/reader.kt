package org.evoleq.math.cat.monad.reader

import org.evoleq.math.cat.marker.MathCatDsl

interface Reader<E, T> : (E)->T {
    infix  fun <S>  map(f: (T)->S): Reader<E, S> = Reader { e ->
        f(this@Reader(e))
    }
    @MathCatDsl
    infix fun runOn(e : E): T = this@Reader(e)
    
    @MathCatDsl
    infix fun <S> bind(f: (T)-> Reader<E, S>): Reader<E, S> = (this map f).multiply()
}

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> Reader(arrow: (E)->T): Reader<E, T> = object : Reader<E, T> {
    override fun invoke(p1: E): T = arrow(p1)
}

interface KlReader<E, K, T> : (K)-> Reader<E, T>

@MathCatDsl
@Suppress("FunctionName")
fun <E, K, T> KlReader(arrow: (K)-> Reader<E, T>): KlReader<E, K, T> = object : KlReader<E, K, T> {
    override fun invoke(p1: K): Reader<E, T> = arrow(p1)
}

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> ReturnReader(t : T): Reader<E, T> = Reader { t }

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> KlReturnReader(): KlReader<E, T, T> = KlReader { t -> ReturnReader(t) }

@MathCatDsl
@Suppress("FunctionName")
fun <E, T> Reader<E, Reader<E, T>>.multiply(): Reader<E, T> = Reader { e ->
    this@multiply runOn e runOn e
}

operator fun <E, K, L, M> KlReader<E, K, L>.times(other: KlReader<E, L, M>): KlReader<E, K, M> = KlReader { k ->
    (this@times(k) map other).multiply()
}
