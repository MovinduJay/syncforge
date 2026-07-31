package com.syncforge.syncforge.conflict.service;

public record ConflictEvaluationResult(
        boolean allowed,
        String message
) {
}
