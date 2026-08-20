package com.finance.tracker.application.handler;

public interface QueryHandler <Q,R> {
    R handle(Q query);
}
