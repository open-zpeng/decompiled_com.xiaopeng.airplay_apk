package javax.servlet.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;
import javax.servlet.ServletOutputStream;
/* compiled from: HttpServlet.java */
/* loaded from: classes.dex */
class NoBodyResponse extends HttpServletResponseWrapper {
    private static final ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings");
    private boolean didSetContentLength;
    private NoBodyOutputStream noBody;
    private boolean usingOutputStream;
    private PrintWriter writer;

    /* JADX INFO: Access modifiers changed from: package-private */
    public NoBodyResponse(HttpServletResponse r) {
        super(r);
        this.noBody = new NoBodyOutputStream();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setContentLength() {
        if (!this.didSetContentLength) {
            if (this.writer != null) {
                this.writer.flush();
            }
            setContentLength(this.noBody.getContentLength());
        }
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public void setContentLength(int len) {
        super.setContentLength(len);
        this.didSetContentLength = true;
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.writer != null) {
            throw new IllegalStateException(lStrings.getString("err.ise.getOutputStream"));
        }
        this.usingOutputStream = true;
        return this.noBody;
    }

    @Override // javax.servlet.ServletResponseWrapper, javax.servlet.ServletResponse
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (this.usingOutputStream) {
            throw new IllegalStateException(lStrings.getString("err.ise.getWriter"));
        }
        if (this.writer == null) {
            OutputStreamWriter w = new OutputStreamWriter(this.noBody, getCharacterEncoding());
            this.writer = new PrintWriter(w);
        }
        return this.writer;
    }
}
