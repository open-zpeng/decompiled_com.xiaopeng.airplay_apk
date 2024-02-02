package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
/* loaded from: classes.dex */
public class Connection {

    /* loaded from: classes.dex */
    public enum Error {
        ERROR_NONE,
        ERROR_COMMAND_ABORTED,
        ERROR_NOT_ENABLED_FOR_INTERNET,
        ERROR_USER_DISCONNECT,
        ERROR_ISP_DISCONNECT,
        ERROR_IDLE_DISCONNECT,
        ERROR_FORCED_DISCONNECT,
        ERROR_NO_CARRIER,
        ERROR_IP_CONFIGURATION,
        ERROR_UNKNOWN
    }

    /* loaded from: classes.dex */
    public enum Status {
        Unconfigured,
        Connecting,
        Connected,
        PendingDisconnect,
        Disconnecting,
        Disconnected
    }

    /* loaded from: classes.dex */
    public enum Type {
        Unconfigured,
        IP_Routed,
        IP_Bridged
    }

    /* loaded from: classes.dex */
    public static class StatusInfo {
        private Error lastError;
        private Status status;
        private long uptimeSeconds;

        public StatusInfo(Status status, UnsignedIntegerFourBytes uptime, Error lastError) {
            this(status, uptime.getValue().longValue(), lastError);
        }

        public StatusInfo(Status status, long uptimeSeconds, Error lastError) {
            this.status = status;
            this.uptimeSeconds = uptimeSeconds;
            this.lastError = lastError;
        }

        public Status getStatus() {
            return this.status;
        }

        public long getUptimeSeconds() {
            return this.uptimeSeconds;
        }

        public UnsignedIntegerFourBytes getUptime() {
            return new UnsignedIntegerFourBytes(getUptimeSeconds());
        }

        public Error getLastError() {
            return this.lastError;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StatusInfo that = (StatusInfo) o;
            if (this.uptimeSeconds == that.uptimeSeconds && this.lastError == that.lastError && this.status == that.status) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            int result = this.status.hashCode();
            return (31 * ((31 * result) + ((int) (this.uptimeSeconds ^ (this.uptimeSeconds >>> 32))))) + this.lastError.hashCode();
        }

        public String toString() {
            return "(" + getClass().getSimpleName() + ") " + getStatus();
        }
    }
}
