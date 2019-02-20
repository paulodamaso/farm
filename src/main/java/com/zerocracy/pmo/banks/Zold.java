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
package com.zerocracy.pmo.banks;

import com.jcabi.aspects.Tv;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.zerocracy.Farm;
import com.zerocracy.cash.Cash;
import com.zerocracy.farm.props.Props;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Zold payment.
 * @since 1.0
 */
public final class Zold implements Bank {

    /**
     * Props.
     */
    private final Props props;

    /**
     * Ctor.
     *
     * @param farm Farm
     */
    public Zold(final Farm farm) {
        this(new Props(farm));
    }

    /**
     * Ctor.
     *
     * @param props Props
     */
    public Zold(final Props props) {
        this.props = props;
    }

    @Override
    public Cash fee(final Cash amount) {
        return Cash.ZERO;
    }

    @Override
    // @checkstyle ParameterNumberCheck (3 lines)
    public String pay(final String target, final Cash amount,
        final String details, final String unique) throws IOException {
        final String uri = this.props.get("//zold/host");
        final RestResponse rsp = new JdkRequest(uri)
            .uri()
            .path("/do-pay")
            .back()
            .method("POST")
            .header("X-Zold-Wts", this.props.get("//zold/secret"))
            .body()
            .formParam("bnf", target)
            .formParam("amount", amount.decimal().toString())
            .formParam("details", new ZoldDetails(details))
            .formParam("keygap", this.props.get("//zold/keygap"))
            .back()
            .fetch()
            .as(RestResponse.class);
        if (rsp.status() != HttpURLConnection.HTTP_MOVED_TEMP) {
            throw new IOException(
                String.format("Zold payment failed, code=%d", rsp.status())
            );
        }
        final List<String> hds = rsp.headers().get("X-Zold-Job");
        if (hds.isEmpty()) {
            throw new IOException(
                "Zold response doesn't have job-id"
            );
        }
        final String job = hds.get(0);
        String status;
        do {
            try {
                TimeUnit.SECONDS.sleep((long) Tv.FIVE);
            } catch (final InterruptedException err) {
                Thread.currentThread().interrupt();
                throw new IOException("Thread interrupted", err);
            }
            final RestResponse jrsp = new JdkRequest(uri)
                .uri().path("/job").queryParam("id", job).back()
                .method("GET")
                .fetch()
                .as(RestResponse.class);
            status = jrsp.body();
            if (jrsp.status() != HttpURLConnection.HTTP_OK) {
                throw new IOException(
                    String.format(
                        "WTS job failed; job-id=%s code=%d status=%s",
                        job, jrsp.status(), status
                    )
                );
            }
        } while ("RUNNING".equalsIgnoreCase(status));
        if (!"OK".equals(status)) {
            throw new IOException(
                String.format(
                    "Failed to pay via WTS: job=%s error=%s",
                    job, status
                )
            );
        }
        return job;
    }

    @Override
    public void close() throws IOException {
        // Nothing to do
    }
}
