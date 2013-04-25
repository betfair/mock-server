/*
 * Copyright (c) 2012 The Sporting Exchange Limited
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1.	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.	Neither the names of The Sporting Exchange Limited, Betfair Limited nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



package com.betfair.utils.mockserver.util

/**
 * User: graya2
 * transcribed from NatPryce's blog
 * Date: 26/09/12
 */
abstract class Maybe<T> implements Iterable<T> {

    abstract def Boolean isKnown()
    abstract def T otherwise(T defaultValue)
    abstract def T otherwise(Closure<T> defaultValue)
    abstract def Maybe<T> otherwise(Maybe<T> maybeDefaultValue)
    abstract def <V> Maybe<V> collect(Closure<V> fn)

    public static <T> Maybe<T> unknown() {
        new Maybe() {
            def Boolean isKnown() { false }
            def Iterator iterator() { Collections.emptyList().iterator() }
            def otherwise(Closure defaultValue) { return defaultValue.call() }
            def Maybe otherwise(Maybe maybeDefaultValue) { return maybeDefaultValue }
            def otherwise(Object defaultValue) { return defaultValue }

            def <V> Maybe<V> collect(Closure<V> fn) { unknown() }

            def String toString() { "unknown" }
            public boolean equals(Object o) { if (o instanceof Maybe) !((Maybe)o).isKnown() else false }
            public int hashCode() { 0 }
        }
    }

    public static <T> Maybe<T> definitely(final T theValue) { new DefiniteValue<T>(theValue) }

    private static class DefiniteValue<T> extends Maybe<T> {
        private final T theValue

        public DefiniteValue(T theValue) { this.theValue = theValue }

        def Boolean isKnown() { true }
        def Iterator iterator() { Collections.singleton(theValue).iterator() }
        def T otherwise(Closure<T> defaultValue) { theValue }
        def Maybe<T> otherwise(Maybe<T> maybeDefaultValue) { this }
        def T otherwise(T defaultValue) { theValue }
        def String toString() { "definitely " + theValue.toString() }
        def <V> Maybe<V> collect(Closure<V> fn) { definitely(fn.call(theValue)) }

        public boolean equals(Object o) {
            if (this == o) return true
            if (o == null || getClass() != o.getClass()) return false
            DefiniteValue that = (DefiniteValue) o
            theValue.equals(that.theValue)
        }
        public int hashCode() { theValue.hashCode() }
    }

    public static <K,V> Maybe<V> safeGet(Map<K,V> map, Object key) {
        map.containsKey(key) ? definitely(map.get(key)) : unknown()
    }
    public static <K,V> Maybe<V> safeRemove(Map<K,V> map, Object key) {
        map.containsKey(key) ? definitely(map.remove(key)) : unknown()
    }
}
