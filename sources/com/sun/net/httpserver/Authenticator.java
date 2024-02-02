package com.sun.net.httpserver;
/* loaded from: classes.dex */
public abstract class Authenticator {

    /* loaded from: classes.dex */
    public static abstract class Result {
    }

    public abstract Result authenticate(HttpExchange httpExchange);

    /* loaded from: classes.dex */
    public static class Failure extends Result {
        private int responseCode;

        public Failure(int i) {
            this.responseCode = i;
        }

        public int getResponseCode() {
            return this.responseCode;
        }
    }

    /* loaded from: classes.dex */
    public static class Success extends Result {
        private HttpPrincipal principal;

        public Success(HttpPrincipal httpPrincipal) {
            this.principal = httpPrincipal;
        }

        public HttpPrincipal getPrincipal() {
            return this.principal;
        }
    }

    /* loaded from: classes.dex */
    public static class Retry extends Result {
        private int responseCode;

        public Retry(int i) {
            this.responseCode = i;
        }

        public int getResponseCode() {
            return this.responseCode;
        }
    }
}
