package org.eclipse.jetty.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
/* loaded from: classes.dex */
public class MultiPartInputStream {
    private static final Logger LOG = Log.getLogger(MultiPartInputStream.class);
    public static final MultipartConfigElement __DEFAULT_MULTIPART_CONFIG = new MultipartConfigElement(System.getProperty("java.io.tmpdir"));
    protected MultipartConfigElement _config;
    protected String _contentType;
    protected File _contextTmpDir;
    protected boolean _deleteOnExit;
    protected InputStream _in;
    protected MultiMap<String> _parts;
    protected File _tmpDir;

    /* loaded from: classes.dex */
    public class MultiPart implements Part {
        protected ByteArrayOutputStream2 _bout;
        protected String _contentType;
        protected File _file;
        protected String _filename;
        protected MultiMap<String> _headers;
        protected String _name;
        protected OutputStream _out;
        protected long _size = 0;
        protected boolean _temporary = true;

        public MultiPart(String name, String filename) throws IOException {
            this._name = name;
            this._filename = filename;
        }

        protected void setContentType(String contentType) {
            this._contentType = contentType;
        }

        protected void open() throws IOException {
            if (this._filename != null && this._filename.trim().length() > 0) {
                createFile();
                return;
            }
            ByteArrayOutputStream2 byteArrayOutputStream2 = new ByteArrayOutputStream2();
            this._bout = byteArrayOutputStream2;
            this._out = byteArrayOutputStream2;
        }

        protected void close() throws IOException {
            this._out.close();
        }

        protected void write(int b) throws IOException {
            if (MultiPartInputStream.this._config.getMaxFileSize() > 0 && this._size + 1 > MultiPartInputStream.this._config.getMaxFileSize()) {
                throw new IllegalStateException("Multipart Mime part " + this._name + " exceeds max filesize");
            }
            if (MultiPartInputStream.this._config.getFileSizeThreshold() > 0 && this._size + 1 > MultiPartInputStream.this._config.getFileSizeThreshold() && this._file == null) {
                createFile();
            }
            this._out.write(b);
            this._size++;
        }

        protected void write(byte[] bytes, int offset, int length) throws IOException {
            if (MultiPartInputStream.this._config.getMaxFileSize() > 0 && this._size + length > MultiPartInputStream.this._config.getMaxFileSize()) {
                throw new IllegalStateException("Multipart Mime part " + this._name + " exceeds max filesize");
            }
            if (MultiPartInputStream.this._config.getFileSizeThreshold() > 0 && this._size + length > MultiPartInputStream.this._config.getFileSizeThreshold() && this._file == null) {
                createFile();
            }
            this._out.write(bytes, offset, length);
            this._size += length;
        }

        protected void createFile() throws IOException {
            this._file = File.createTempFile("MultiPart", "", MultiPartInputStream.this._tmpDir);
            this._file.setReadable(false, false);
            this._file.setReadable(true, true);
            if (MultiPartInputStream.this._deleteOnExit) {
                this._file.deleteOnExit();
            }
            FileOutputStream fos = new FileOutputStream(this._file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            if (this._size > 0 && this._out != null) {
                this._out.flush();
                this._bout.writeTo(bos);
                this._out.close();
                this._bout = null;
            }
            this._out = bos;
        }

        protected void setHeaders(MultiMap<String> headers) {
            this._headers = headers;
        }

        @Override // javax.servlet.http.Part
        public String getContentType() {
            return this._contentType;
        }

        @Override // javax.servlet.http.Part
        public String getHeader(String name) {
            if (name == null) {
                return null;
            }
            return (String) this._headers.getValue(name.toLowerCase(Locale.ENGLISH), 0);
        }

        @Override // javax.servlet.http.Part
        public Collection<String> getHeaderNames() {
            return this._headers.keySet();
        }

        @Override // javax.servlet.http.Part
        public Collection<String> getHeaders(String name) {
            return this._headers.getValues(name);
        }

        @Override // javax.servlet.http.Part
        public InputStream getInputStream() throws IOException {
            if (this._file != null) {
                return new BufferedInputStream(new FileInputStream(this._file));
            }
            return new ByteArrayInputStream(this._bout.getBuf(), 0, this._bout.size());
        }

        public byte[] getBytes() {
            if (this._bout != null) {
                return this._bout.toByteArray();
            }
            return null;
        }

        @Override // javax.servlet.http.Part
        public String getName() {
            return this._name;
        }

        @Override // javax.servlet.http.Part
        public long getSize() {
            return this._size;
        }

        @Override // javax.servlet.http.Part
        public void write(String fileName) throws IOException {
            if (this._file == null) {
                this._temporary = false;
                this._file = new File(MultiPartInputStream.this._tmpDir, fileName);
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(this._file));
                    this._bout.writeTo(bos);
                    bos.flush();
                    bos.close();
                    this._bout = null;
                    return;
                } catch (Throwable th) {
                    if (bos != null) {
                        bos.close();
                    }
                    this._bout = null;
                    throw th;
                }
            }
            this._temporary = false;
            File f = new File(MultiPartInputStream.this._tmpDir, fileName);
            if (this._file.renameTo(f)) {
                this._file = f;
            }
        }

        @Override // javax.servlet.http.Part
        public void delete() throws IOException {
            if (this._file != null && this._file.exists()) {
                this._file.delete();
            }
        }

        public void cleanUp() throws IOException {
            if (this._temporary && this._file != null && this._file.exists()) {
                this._file.delete();
            }
        }

        public File getFile() {
            return this._file;
        }

        public String getContentDispositionFilename() {
            return this._filename;
        }
    }

    public MultiPartInputStream(InputStream in, String contentType, MultipartConfigElement config, File contextTmpDir) {
        this._in = new ReadLineInputStream(in);
        this._contentType = contentType;
        this._config = config;
        this._contextTmpDir = contextTmpDir;
        if (this._contextTmpDir == null) {
            this._contextTmpDir = new File(System.getProperty("java.io.tmpdir"));
        }
        if (this._config == null) {
            this._config = new MultipartConfigElement(this._contextTmpDir.getAbsolutePath());
        }
    }

    public Collection<Part> getParsedParts() {
        if (this._parts == null) {
            return Collections.emptyList();
        }
        Collection<Object> values = this._parts.values();
        List<Part> parts = new ArrayList<>();
        for (Object o : values) {
            List<Part> asList = LazyList.getList(o, false);
            parts.addAll(asList);
        }
        return parts;
    }

    public void deleteParts() throws MultiException {
        Collection<Part> parts = getParsedParts();
        MultiException err = new MultiException();
        for (Part p : parts) {
            try {
                ((MultiPart) p).cleanUp();
            } catch (Exception e) {
                err.add(e);
            }
        }
        this._parts.clear();
        err.ifExceptionThrowMulti();
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        parse();
        Collection<Object> values = this._parts.values();
        List<Part> parts = new ArrayList<>();
        for (Object o : values) {
            List<Part> asList = LazyList.getList(o, false);
            parts.addAll(asList);
        }
        return parts;
    }

    public Part getPart(String name) throws IOException, ServletException {
        parse();
        return (Part) this._parts.getValue(name, 0);
    }

    /* JADX WARN: Code restructure failed: missing block: B:100:0x0222, code lost:
        r7 = new java.lang.StringBuilder();
        r7.append("Request exceeds maxRequestSize (");
     */
    /* JADX WARN: Code restructure failed: missing block: B:102:0x0232, code lost:
        r7.append(r35._config.getMaxRequestSize());
        r7.append(")");
     */
    /* JADX WARN: Code restructure failed: missing block: B:103:0x0245, code lost:
        throw new java.lang.IllegalStateException(r7.toString());
     */
    /* JADX WARN: Code restructure failed: missing block: B:104:0x0246, code lost:
        r34 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:105:0x0248, code lost:
        r12 = -2;
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:106:0x024d, code lost:
        if (r6 == 13) goto L194;
     */
    /* JADX WARN: Code restructure failed: missing block: B:107:0x024f, code lost:
        if (r6 != 10) goto L119;
     */
    /* JADX WARN: Code restructure failed: missing block: B:109:0x0252, code lost:
        if (r0 < 0) goto L120;
     */
    /* JADX WARN: Code restructure failed: missing block: B:111:0x0255, code lost:
        if (r0 >= r8.length) goto L120;
     */
    /* JADX WARN: Code restructure failed: missing block: B:113:0x0259, code lost:
        if (r6 != r8[r0]) goto L120;
     */
    /* JADX WARN: Code restructure failed: missing block: B:114:0x025b, code lost:
        r0 = r0 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:115:0x025d, code lost:
        r6 = r28;
        r7 = r30;
        r10 = r31;
        r11 = r34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:116:0x0266, code lost:
        if (r16 == false) goto L122;
     */
    /* JADX WARN: Code restructure failed: missing block: B:117:0x0268, code lost:
        r0.write(13);
     */
    /* JADX WARN: Code restructure failed: missing block: B:118:0x026d, code lost:
        if (r13 == false) goto L124;
     */
    /* JADX WARN: Code restructure failed: missing block: B:119:0x026f, code lost:
        r0.write(10);
     */
    /* JADX WARN: Code restructure failed: missing block: B:120:0x0272, code lost:
        r13 = false;
        r16 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:121:0x0276, code lost:
        if (r0 <= 0) goto L127;
     */
    /* JADX WARN: Code restructure failed: missing block: B:122:0x0278, code lost:
        r0.write(r8, 0, r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:123:0x027b, code lost:
        r0 = -1;
        r0.write(r6);
     */
    /* JADX WARN: Code restructure failed: missing block: B:125:0x0282, code lost:
        if (r6 != 13) goto L150;
     */
    /* JADX WARN: Code restructure failed: missing block: B:126:0x0284, code lost:
        r4.mark(1);
        r11 = r4.read();
     */
    /* JADX WARN: Code restructure failed: missing block: B:127:0x028c, code lost:
        if (r11 == 10) goto L149;
     */
    /* JADX WARN: Code restructure failed: missing block: B:128:0x028e, code lost:
        r4.reset();
     */
    /* JADX WARN: Code restructure failed: missing block: B:129:0x0292, code lost:
        r12 = r11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:131:0x0295, code lost:
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:133:0x029a, code lost:
        r34 = r11;
        r6 = r29;
     */
    /* JADX WARN: Code restructure failed: missing block: B:134:0x029e, code lost:
        if (r0 <= 0) goto L192;
     */
    /* JADX WARN: Code restructure failed: missing block: B:136:0x02a3, code lost:
        if (r0 < (r8.length - 2)) goto L154;
     */
    /* JADX WARN: Code restructure failed: missing block: B:138:0x02a6, code lost:
        r21 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:139:0x02a9, code lost:
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:141:0x02ac, code lost:
        r21 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:142:0x02b1, code lost:
        if (r0 != (r8.length - 1)) goto L160;
     */
    /* JADX WARN: Code restructure failed: missing block: B:143:0x02b3, code lost:
        if (r16 == false) goto L157;
     */
    /* JADX WARN: Code restructure failed: missing block: B:144:0x02b5, code lost:
        r0.write(13);
     */
    /* JADX WARN: Code restructure failed: missing block: B:145:0x02ba, code lost:
        if (r13 == false) goto L159;
     */
    /* JADX WARN: Code restructure failed: missing block: B:146:0x02bc, code lost:
        r0.write(10);
     */
    /* JADX WARN: Code restructure failed: missing block: B:147:0x02bf, code lost:
        r13 = false;
        r16 = false;
        r0.write(r8, 0, r0);
        r0 = -1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:148:0x02c7, code lost:
        if (r0 > 0) goto L191;
     */
    /* JADX WARN: Code restructure failed: missing block: B:149:0x02c9, code lost:
        if (r6 != (-1)) goto L162;
     */
    /* JADX WARN: Code restructure failed: missing block: B:151:0x02cc, code lost:
        if (r16 == false) goto L164;
     */
    /* JADX WARN: Code restructure failed: missing block: B:152:0x02ce, code lost:
        r0.write(13);
     */
    /* JADX WARN: Code restructure failed: missing block: B:153:0x02d3, code lost:
        if (r13 == false) goto L166;
     */
    /* JADX WARN: Code restructure failed: missing block: B:154:0x02d5, code lost:
        r0.write(10);
     */
    /* JADX WARN: Code restructure failed: missing block: B:156:0x02da, code lost:
        if (r6 != 13) goto L180;
     */
    /* JADX WARN: Code restructure failed: missing block: B:157:0x02dc, code lost:
        r7 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:158:0x02df, code lost:
        r7 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:159:0x02e0, code lost:
        r16 = r7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:160:0x02e2, code lost:
        if (r6 == 10) goto L179;
     */
    /* JADX WARN: Code restructure failed: missing block: B:161:0x02e4, code lost:
        if (r12 != 10) goto L173;
     */
    /* JADX WARN: Code restructure failed: missing block: B:163:0x02e7, code lost:
        r7 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:164:0x02e9, code lost:
        r7 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:165:0x02eb, code lost:
        if (r12 != 10) goto L178;
     */
    /* JADX WARN: Code restructure failed: missing block: B:166:0x02ed, code lost:
        r12 = -2;
     */
    /* JADX WARN: Code restructure failed: missing block: B:167:0x02ef, code lost:
        r0 = r7;
        r6 = r28;
        r7 = r30;
        r10 = r31;
        r11 = r34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:169:0x02fb, code lost:
        if (r0 != r8.length) goto L186;
     */
    /* JADX WARN: Code restructure failed: missing block: B:170:0x02fd, code lost:
        r2 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:171:0x02fe, code lost:
        if (r12 != 10) goto L188;
     */
    /* JADX WARN: Code restructure failed: missing block: B:173:0x0301, code lost:
        r0.close();
        r4 = r20;
        r5 = r21;
        r12 = r26;
        r6 = r28;
        r7 = r30;
        r11 = r34;
     */
    /* JADX WARN: Code restructure failed: missing block: B:174:0x0315, code lost:
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:176:0x031c, code lost:
        r0.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:177:0x031f, code lost:
        throw r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:179:0x0335, code lost:
        throw new java.io.IOException("Missing content-disposition");
     */
    /* JADX WARN: Code restructure failed: missing block: B:57:0x013c, code lost:
        if (r3 == null) goto L218;
     */
    /* JADX WARN: Code restructure failed: missing block: B:58:0x013e, code lost:
        r19 = false;
        r20 = r4;
        r0 = new org.eclipse.jetty.util.QuotedStringTokenizer(r3, ";", r10, r5);
        r10 = null;
        r0 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:59:0x014d, code lost:
        r21 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:60:0x0153, code lost:
        if (r0.hasMoreTokens() == false) goto L94;
     */
    /* JADX WARN: Code restructure failed: missing block: B:61:0x0155, code lost:
        r0 = r0.nextToken().trim();
        r5 = r0.toLowerCase(java.util.Locale.ENGLISH);
        r22 = r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:62:0x016b, code lost:
        if (r0.startsWith("form-data") == false) goto L87;
     */
    /* JADX WARN: Code restructure failed: missing block: B:63:0x016d, code lost:
        r19 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:64:0x0170, code lost:
        r0 = r21;
     */
    /* JADX WARN: Code restructure failed: missing block: B:66:0x0179, code lost:
        if (r5.startsWith("name=") == false) goto L90;
     */
    /* JADX WARN: Code restructure failed: missing block: B:67:0x017b, code lost:
        r10 = value(r0, true);
     */
    /* JADX WARN: Code restructure failed: missing block: B:69:0x0187, code lost:
        if (r5.startsWith("filename=") == false) goto L84;
     */
    /* JADX WARN: Code restructure failed: missing block: B:70:0x0189, code lost:
        r0 = filenameValue(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:71:0x018d, code lost:
        r3 = r22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:73:0x0194, code lost:
        if (r19 != false) goto L97;
     */
    /* JADX WARN: Code restructure failed: missing block: B:75:0x0197, code lost:
        if (r10 != null) goto L98;
     */
    /* JADX WARN: Code restructure failed: missing block: B:76:0x019a, code lost:
        r4 = r20;
        r5 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:77:0x019e, code lost:
        r10 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:78:0x01a1, code lost:
        r0 = new org.eclipse.jetty.util.MultiPartInputStream.MultiPart(r35, r10, r21);
        r0.setHeaders(r16);
        r0.setContentType(r14);
        r35._parts.add(r10, r0);
        r0.open();
     */
    /* JADX WARN: Code restructure failed: missing block: B:79:0x01c4, code lost:
        if ("base64".equalsIgnoreCase(r15) == false) goto L212;
     */
    /* JADX WARN: Code restructure failed: missing block: B:80:0x01c6, code lost:
        r0 = new org.eclipse.jetty.util.MultiPartInputStream.Base64InputStream((org.eclipse.jetty.util.ReadLineInputStream) r35._in);
     */
    /* JADX WARN: Code restructure failed: missing block: B:81:0x01d1, code lost:
        r4 = r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:83:0x01db, code lost:
        if ("quoted-printable".equalsIgnoreCase(r15) == false) goto L215;
     */
    /* JADX WARN: Code restructure failed: missing block: B:84:0x01dd, code lost:
        r0 = new org.eclipse.jetty.util.MultiPartInputStream.AnonymousClass1(r35, r35._in);
     */
    /* JADX WARN: Code restructure failed: missing block: B:85:0x01e5, code lost:
        r0 = r35._in;
     */
    /* JADX WARN: Code restructure failed: missing block: B:86:0x01e8, code lost:
        r16 = false;
        r26 = r12;
        r12 = -2;
        r0 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:87:0x01ef, code lost:
        r13 = r0;
        r0 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:88:0x01f1, code lost:
        r28 = r6;
     */
    /* JADX WARN: Code restructure failed: missing block: B:89:0x01f4, code lost:
        if (r12 == (-2)) goto L207;
     */
    /* JADX WARN: Code restructure failed: missing block: B:90:0x01f6, code lost:
        r6 = r12;
     */
    /* JADX WARN: Code restructure failed: missing block: B:91:0x01f8, code lost:
        r6 = r4.read();
     */
    /* JADX WARN: Code restructure failed: missing block: B:92:0x01fc, code lost:
        r29 = r6;
        r30 = r7;
        r31 = r10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:93:0x0205, code lost:
        if (r6 == (-1)) goto L205;
     */
    /* JADX WARN: Code restructure failed: missing block: B:94:0x0207, code lost:
        r26 = r26 + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:96:0x0213, code lost:
        if (r35._config.getMaxRequestSize() <= 0) goto L202;
     */
    /* JADX WARN: Code restructure failed: missing block: B:98:0x021d, code lost:
        if (r26 > r35._config.getMaxRequestSize()) goto L195;
     */
    /* JADX WARN: Code restructure failed: missing block: B:99:0x021f, code lost:
        r34 = r11;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    protected void parse() throws java.io.IOException, javax.servlet.ServletException {
        /*
            Method dump skipped, instructions count: 1054
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.eclipse.jetty.util.MultiPartInputStream.parse():void");
    }

    public void setDeleteOnExit(boolean deleteOnExit) {
        this._deleteOnExit = deleteOnExit;
    }

    public boolean isDeleteOnExit() {
        return this._deleteOnExit;
    }

    private String value(String nameEqualsValue, boolean splitAfterSpace) {
        int idx = nameEqualsValue.indexOf(61);
        String value = nameEqualsValue.substring(idx + 1).trim();
        return QuotedStringTokenizer.unquoteOnly(value);
    }

    private String filenameValue(String nameEqualsValue) {
        int idx = nameEqualsValue.indexOf(61);
        String value = nameEqualsValue.substring(idx + 1).trim();
        if (value.matches(".??[a-z,A-Z]\\:\\\\[^\\\\].*")) {
            char first = value.charAt(0);
            if (first == '\"' || first == '\'') {
                value = value.substring(1);
            }
            char last = value.charAt(value.length() - 1);
            if (last == '\"' || last == '\'') {
                return value.substring(0, value.length() - 1);
            }
            return value;
        }
        return QuotedStringTokenizer.unquoteOnly(value, true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class Base64InputStream extends InputStream {
        byte[] _buffer;
        ReadLineInputStream _in;
        String _line;
        int _pos;

        public Base64InputStream(ReadLineInputStream rlis) {
            this._in = rlis;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            if (this._buffer == null || this._pos >= this._buffer.length) {
                this._line = this._in.readLine();
                if (this._line == null) {
                    return -1;
                }
                if (this._line.startsWith("--")) {
                    this._buffer = (this._line + "\r\n").getBytes();
                } else if (this._line.length() == 0) {
                    this._buffer = "\r\n".getBytes();
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(((4 * this._line.length()) / 3) + 2);
                    B64Code.decode(this._line, baos);
                    baos.write(13);
                    baos.write(10);
                    this._buffer = baos.toByteArray();
                }
                this._pos = 0;
            }
            byte[] bArr = this._buffer;
            int i = this._pos;
            this._pos = i + 1;
            return bArr[i];
        }
    }
}
