package com.finance.tracker.application.handler;

public interface CommandHandler<C, R> {
    R handle(C command);
}
