package org.gabo.exception;

public class AliasAlreadyExistsException extends DomainException {
    public AliasAlreadyExistsException(String alias) {
        super("Already exists: " + alias);
    }
}
