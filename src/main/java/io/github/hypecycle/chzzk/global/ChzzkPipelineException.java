package io.github.hypecycle.chzzk.global;

import lombok.Getter;

public class ChzzkPipelineException extends RuntimeException {

    private ErrorCode errorCode;

    public ChzzkPipelineException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
