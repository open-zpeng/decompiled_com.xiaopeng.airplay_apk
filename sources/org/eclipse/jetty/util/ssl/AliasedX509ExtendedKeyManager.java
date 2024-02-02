package org.eclipse.jetty.util.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
/* loaded from: classes.dex */
public class AliasedX509ExtendedKeyManager extends X509ExtendedKeyManager {
    private String _keyAlias;
    private X509KeyManager _keyManager;

    public AliasedX509ExtendedKeyManager(String keyAlias, X509KeyManager keyManager) throws Exception {
        this._keyAlias = keyAlias;
        this._keyManager = keyManager;
    }

    @Override // javax.net.ssl.X509KeyManager
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return this._keyAlias == null ? this._keyManager.chooseClientAlias(keyType, issuers, socket) : this._keyAlias;
    }

    @Override // javax.net.ssl.X509KeyManager
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return this._keyAlias == null ? this._keyManager.chooseServerAlias(keyType, issuers, socket) : this._keyAlias;
    }

    @Override // javax.net.ssl.X509KeyManager
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return this._keyManager.getClientAliases(keyType, issuers);
    }

    @Override // javax.net.ssl.X509KeyManager
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return this._keyManager.getServerAliases(keyType, issuers);
    }

    @Override // javax.net.ssl.X509KeyManager
    public X509Certificate[] getCertificateChain(String alias) {
        return this._keyManager.getCertificateChain(alias);
    }

    @Override // javax.net.ssl.X509KeyManager
    public PrivateKey getPrivateKey(String alias) {
        return this._keyManager.getPrivateKey(alias);
    }

    @Override // javax.net.ssl.X509ExtendedKeyManager
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return this._keyAlias == null ? super.chooseEngineServerAlias(keyType, issuers, engine) : this._keyAlias;
    }

    @Override // javax.net.ssl.X509ExtendedKeyManager
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return this._keyAlias == null ? super.chooseEngineClientAlias(keyType, issuers, engine) : this._keyAlias;
    }
}
