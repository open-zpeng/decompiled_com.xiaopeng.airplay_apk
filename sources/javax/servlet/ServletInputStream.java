package javax.servlet;

import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public abstract class ServletInputStream extends InputStream {
    public int readLine(byte[] b, int off, int len) throws IOException {
        int count = 0;
        if (len <= 0) {
            return 0;
        }
        while (true) {
            int c = read();
            if (c == -1) {
                break;
            }
            int off2 = off + 1;
            b[off] = (byte) c;
            count++;
            if (c == 10 || count == len) {
                break;
            }
            off = off2;
        }
        if (count > 0) {
            return count;
        }
        return -1;
    }
}
