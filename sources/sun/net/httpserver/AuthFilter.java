package sun.net.httpserver;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public class AuthFilter extends Filter {
    private Authenticator authenticator;

    public AuthFilter(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override // com.sun.net.httpserver.Filter
    public String description() {
        return "Authentication filter";
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void consumeInput(HttpExchange httpExchange) throws IOException {
        InputStream requestBody = httpExchange.getRequestBody();
        do {
        } while (requestBody.read(new byte[4096]) != -1);
        requestBody.close();
    }

    @Override // com.sun.net.httpserver.Filter
    public void doFilter(HttpExchange httpExchange, Filter.Chain chain) throws IOException {
        if (this.authenticator != null) {
            Authenticator.Result authenticate = this.authenticator.authenticate(httpExchange);
            if (authenticate instanceof Authenticator.Success) {
                ExchangeImpl.get(httpExchange).setPrincipal(((Authenticator.Success) authenticate).getPrincipal());
                chain.doFilter(httpExchange);
                return;
            } else if (authenticate instanceof Authenticator.Retry) {
                consumeInput(httpExchange);
                httpExchange.sendResponseHeaders(((Authenticator.Retry) authenticate).getResponseCode(), -1L);
                return;
            } else if (authenticate instanceof Authenticator.Failure) {
                consumeInput(httpExchange);
                httpExchange.sendResponseHeaders(((Authenticator.Failure) authenticate).getResponseCode(), -1L);
                return;
            } else {
                return;
            }
        }
        chain.doFilter(httpExchange);
    }
}
