package org.gabo.exception;

public class AliasNotFoundException extends DomainException {
    public AliasNotFoundException(String alias) {
        super("Alias not found: " + alias);
    }
}
