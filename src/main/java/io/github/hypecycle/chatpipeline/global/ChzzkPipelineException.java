package io.github.hypecycle.chatpipeline.global;

public class ChzzkPipelineException extends RuntimeException {

    private ErrorCode errorCode;

    public ChzzkPipelineException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
