/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 */

package com.oracle.iot.util;

import sun.security.x509.X500Name;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.logging.Logger;

// This file is a copy of 
// Device/TrustManagerCommon/src/main/java/com/oracle/iot/trust/util/IoTHostnameVerifier.java
// from the iot repository.
// Accurate as of 15/08/03
/**
 * Hostname verifier for IoT Client Library
 */
public class IoTHostnameVerifier implements HostnameVerifier {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    String serverHost = null;
    String serverCn = null;

    public IoTHostnameVerifier(String hostName, String serverCn) {
        this.serverHost = hostName;
        this.serverCn = serverCn;
    }

    @Override
    public boolean verify(String verifyHostName, SSLSession sslSession) {
        boolean isVerified = false;
        boolean hasFailures = false;

        if (serverHost == null) {
            hasFailures = true;
            logger.severe("server.host is not set.");
        } else if (!serverHost.equals(verifyHostName)) {
            hasFailures = true;
            logger.severe("server.host is not correct host.");
        }

        if (serverCn == null) {
            hasFailures = true;
            logger.severe("server.cn is not set.");
        }

        if (!hasFailures) {
            try {
                String peerPrincipal = sslSession.getPeerPrincipal().getName();

                try {
                    X500Name x500Name = new X500Name(peerPrincipal);
                    String commonName = x500Name.getCommonName();

                    if (commonName != null) {
                        // The server.cn property gets trimmed, so we need to trim the CN we get from the cert to match.
                        commonName = commonName.trim();
                        // TODO: We need to validate the chain (see IOT-10300)
                        // TODO: We will add additional checks here:
                        //   - Wildcard names
                        //   - Domain names
                        //   - Other
                        if (commonName.equals(serverCn)) {
                            isVerified = true;
                        }
                    } else {
                        logger.severe("Error getting common name.");
                    }
                } catch (IOException ignore) {
                    logger.severe("Error parsing principal.");
                }
            } catch (SSLPeerUnverifiedException ignore) {
                logger.severe("Peer host name identity has not been verified.");
            }
        }

        return isVerified;
    }
}
