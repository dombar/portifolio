package br.com.portifolio.exception;

public class InvalidStatusTransitionException extends BusinessRuleException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
