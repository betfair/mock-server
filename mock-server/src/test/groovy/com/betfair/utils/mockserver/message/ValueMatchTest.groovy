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



package com.betfair.utils.mockserver.message;

import org.hamcrest.Matcher;
import org.junit.Test;


import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat

/**
 * User: graya2
 * Date: 10/09/12
 */
public class ValueMatchTest {

    ValueMatch tested = new ValueMatch();

    @Test
    public void shouldMatchAnEq() throws Exception {
        tested.eq = "moo";

        Matcher rule = tested.asMatcher();
        assertThat("moo", rule);
        assertThat("mOo", not(rule));
    }

    @Test
    public void shouldMatchAnEqI() throws Exception {
        tested.eqI = "Moo"
        Matcher rule = tested.asMatcher();
        assertThat("moo", rule);
        assertThat("Moo", rule);
        assertThat("mOO", rule);
        assertThat("Mood", not(rule))
        assertThat(" Moo", not(rule))
        assertThat("Moo ", not(rule))
    }

    @Test
    public void shouldMatchABegins() throws Exception {
        tested.beginsWith = "moo";
        Matcher rule = tested.asMatcher();
        assertThat("moo", rule);
        assertThat("moodle", rule);
        assertThat("poodle", not(rule));
    }

    @Test
    public void shouldMatchAnEnds() throws Exception {
        tested.endsWith = "moo";
        Matcher rule = tested.asMatcher();
        assertThat("moo", rule);
        assertThat("Big-moo", rule);
        assertThat("poodle", not(rule));
    }

    @Test
    public void shouldMatchAnContains() throws Exception {
        tested.endsWith = "moo";
        Matcher rule = tested.asMatcher();
        assertThat("moo", rule);
        assertThat("Big-moo", rule);
        assertThat("poodle", not(rule));
    }

    @Test
    public void shouldMatchARegex() throws Exception {
        tested.regex = "mo+";
        Matcher rule = tested.asMatcher();
        assertThat("mo", rule);
        assertThat("mooooooooo", rule);
        assertThat("amoo", not(rule));
        assertThat("moOo", not(rule));
    }

    @Test
    public void shouldMatchWithAndCombinations() throws Exception {

        tested.all = asList(new ValueMatch() {{beginsWith = "ab";}}, new ValueMatch() {{endsWith = "yz";}});

        Matcher rule = tested.asMatcher();
        assertThat("abcxyz", rule);
        assertThat("abyz", rule);
        assertThat("aabxyx", not(rule));
        assertThat("moOo", not(rule));
    }

    @Test
    public void shouldMatchWithOrCombinations() throws Exception {

        tested.any = asList(new ValueMatch() {{beginsWith = "ab";}}, new ValueMatch() {{beginsWith = "yz";}});

        Matcher rule = tested.asMatcher();
        assertThat("abcxyz", rule);
        assertThat("yzlkasdfuh", rule);
        assertThat("aabxyz", not(rule));
    }



}
