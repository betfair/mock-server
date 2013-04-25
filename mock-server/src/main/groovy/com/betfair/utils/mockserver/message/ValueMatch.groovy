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



package com.betfair.utils.mockserver.message

import com.betfair.utils.mockserver.util.MockMatchers
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.text.IsEqualIgnoringCase
import org.hamcrest.TypeSafeMatcher

import java.util.regex.Pattern

class ValueMatch<T extends ValueMatch> extends AggregateMatch<String, T> implements MatcherBuilder<String> {
    String eq, eqI, beginsWith, endsWith, contains, regex
    List<String> isIn

    public Matcher<String> asMatcher() {

        LinkedList<Matcher<String>> all = new LinkedList<Matcher<String>>();

        all.addAll(addInMatchers())
        all.addAll(combined())

        return MockMatchers.condenseGroup(all)
    }

    private LinkedList<Matcher<String>> addInMatchers() {
        LinkedList<Matcher<String>> all = new LinkedList<Matcher<String>>();
        if (eq != null) {
            all.add(CoreMatchers.equalTo(eq))
        }
        if (eqI != null) {
            all.add(IsEqualIgnoringCase.equalToIgnoringCase(eqI))
        }
        if (beginsWith != null) {
            all.add(CoreMatchers.startsWith(beginsWith))
        }
        if (endsWith != null) {
            all.add(CoreMatchers.endsWith(endsWith))
        }
        if (contains != null) {
            all.add(CoreMatchers.containsString(contains))
        }
        if (regex != null) {
            all.add(new RegexMatcher(regex))
        }
        all
    }

    public class RegexMatcher extends TypeSafeMatcher<String> {
        private final Pattern pattern;

        RegexMatcher(String pattern) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        protected boolean matchesSafely(String item) {
            return pattern.matcher(item).matches()
        }

        @Override
        void describeTo(Description description) {
            description.appendText("matches regex ").appendText(pattern.pattern())
        }
    }

}
