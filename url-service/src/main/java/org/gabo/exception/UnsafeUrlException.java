package org.gabo.exception;

public class UnsafeUrlException extends DomainException {
    public UnsafeUrlException(String url, String reason) {
        super(String.format("%s is not safe, %s", url, reason));
    }
}
