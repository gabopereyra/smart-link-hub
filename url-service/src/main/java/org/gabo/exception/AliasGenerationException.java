package org.gabo.exception;

public class AliasGenerationException extends DomainException {
    public AliasGenerationException() {
        super("Could not generate a unique alias, please try again later");
    }
}