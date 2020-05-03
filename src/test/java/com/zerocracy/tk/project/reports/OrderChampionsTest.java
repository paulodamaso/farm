/*
 * Copyright (c) 2016-2019 Zerocracy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to read
 * the Software only. Permissions is hereby NOT GRANTED to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zerocracy.tk.project.reports;

import com.jcabi.xml.XML;
import com.zerocracy.FkFarm;
import com.zerocracy.FkProject;
import com.zerocracy.Project;
import com.zerocracy.claims.ClaimOut;
import com.zerocracy.claims.ClaimsItem;
import com.zerocracy.claims.Footprint;
import com.zerocracy.entry.ClaimsOf;
import java.time.Instant;
import org.bson.Document;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link OrderChampions}.
 * @since 1.0
 * @checkstyle JavadocMethodCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class OrderChampionsTest {

    @Test
    public void retrievesReportData() throws Exception {
        final Project pkt = new FkProject("7460A2829");
        new ClaimOut()
            .type("Order was given")
            .param("login", "yegor256")
            .postTo(new ClaimsOf(FkFarm.props(), pkt));
        final XML xml = new ClaimsItem(pkt).iterate().iterator().next();
        try (final Footprint footprint = new Footprint(FkFarm.props(), pkt)) {
            footprint.open(xml, "test");
            final Iterable<Document> docs = footprint.collection().aggregate(
                new OrderChampions().bson(
                    pkt,
                    Instant.ofEpochMilli(0L),
                    Instant.now()
                )
            );
            MatcherAssert.assertThat(docs, Matchers.iterableWithSize(1));
            MatcherAssert.assertThat(
                docs.iterator().next().get("user"),
                Matchers.equalTo("@yegor256")
            );
        }
    }
}
