package org.gabo.exception;

public class AliasAlreadyInactiveException extends DomainException {
    public AliasAlreadyInactiveException(String alias) {
        super("Already exists: " + alias);
    }
}