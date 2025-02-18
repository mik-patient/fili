// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.logging.blocks

import com.yahoo.bard.webservice.application.ObjectMappersSuite
import com.yahoo.bard.webservice.util.GroovyTestUtils

import org.junit.Ignore

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.atomic.AtomicLong

class BardQueryInfoSpec extends Specification {
    BardQueryInfo bardQueryInfo

    def setup() {
        bardQueryInfo = BardQueryInfoUtils.initializeBardQueryInfo()
    }

    def cleanup() {
        BardQueryInfoUtils.resetBardQueryInfo()
    }

    def "getBardQueryInfo() returns registered BardQueryInfo instance"() {
        expect:
        BardQueryInfo.getBardQueryInfo() == bardQueryInfo
    }

    def "BardCacheInfo with no weight check is serialized correctly"() {
        when:
        BardCacheInfo bardCacheInfo = new BardCacheInfo("testsetFailureSerialization", 10, "test", null,100)

        then:
        GroovyTestUtils.compareJson(
                new ObjectMappersSuite().jsonMapper.writeValueAsString(bardCacheInfo),
                """{"opType":"testsetFailureSerialization","cacheKeyCksum":"test","signatureCksum":null,"cacheKeyLen":10,"cacheValLen":100}"""
        )
    }

    def "BardQueryInfo with weight check is serialized correctly"() {
        when:
        BardQueryInfo.incrementCountWeightCheck()
        BardQueryInfo.accumulateSketchesScanned( 5000 * 5)
        BardQueryInfo.accumulateLinesScanned(5000L)
        BardQueryInfo.accumulateLinesOutput(100)
        BardQueryInfo.accumulateSketchesOutput(500L )

        then:
        GroovyTestUtils.compareJson(
                new ObjectMappersSuite().jsonMapper.writeValueAsString(bardQueryInfo),
                """{"type":"test","queryCounter":{"factCacheHits":0,"factCachePutErrors":0,"factCachePutTimeouts":0,"factQueryCount":0,""" +
                """"weightCheckLinesOutput":100,"weightCheckLinesScanned":5000,"weightCheckQueries":1,"weightCheckSketchesOutput":500,"weightCheckSketchesScanned":25000},"cacheStats":[]}"""
        )
    }

    @Ignore
    @Unroll
    def "Validate cachePutFailures LogInfo is serialized correctly"() {
        when:
        bardQueryInfo.addCacheInfo("test", new BardCacheInfo("setFailure", 10, "test", "testSignature",100))

        then:
        String serialized = new ObjectMappersSuite().jsonMapper.writeValueAsString(bardQueryInfo)
        GroovyTestUtils.compareJson(
                serialized,
                """{"type":"test",""" +
                        """"queryCounter":{"factCacheHits":0,"factCachePutErrors":0,"factCachePutTimeouts":0,"factQueryCount":0,"weightCheckQueries":0}""" +
                        ""","cacheStats":[{"opType":"setFailure","cacheKeyCksum":"test","signatureCksum":"testSignature","cacheKeyLen":10,"cacheValLen":100}]""" +
                        """}"""
        )
    }

    @Unroll
    def "increment Count For #queryType increments counter by 1"() {
        setup:
        AtomicLong counter = BardQueryInfo.bardQueryInfo.queryCounter.get(queryType);

        expect: "count for #queryType is 0"
        counter.get() == 0

        when: "calling incrementCountFor(#queryType)"
        incrementor()

        then: "count of #queryType is incremented by 1"
        counter.get().longValue() == 1

        where:
        queryType                          | incrementor
        BardQueryInfo.WEIGHT_CHECK         | BardQueryInfo.&incrementCountWeightCheck
        BardQueryInfo.FACT_QUERIES         | BardQueryInfo.&incrementCountFactHits
        BardQueryInfo.FACT_QUERY_CACHE_HIT | BardQueryInfo.&incrementCountCacheHits
        BardQueryInfo.FACT_PUT_ERRORS      | BardQueryInfo.&incrementCountCacheSetFailures
        BardQueryInfo.FACT_PUT_TIMEOUTS    | BardQueryInfo.&incrementCountCacheSetTimeoutFailures
    }

    @Ignore
    def "Object serializes with type and map"() {
        expect:
        GroovyTestUtils.compareJson(
                new ObjectMappersSuite().jsonMapper.writeValueAsString(bardQueryInfo),
                """{"type":"test","queryCounter":{"factCacheHits":0,"factCachePutErrors":0,"factCachePutTimeouts":0,"factQueryCount":0,"weightCheckQueries":0},"cacheStats":[]}"""
        )
    }
}
