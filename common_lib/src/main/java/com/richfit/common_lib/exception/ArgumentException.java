package com.richfit.common_lib.exception;

/**
 * 参数错误
 * @version monday 2016-03
 */
public class ArgumentException extends Exception {

    public ArgumentException() {
    }

    public ArgumentException(String message) {
        super(message);
    }

    public ArgumentException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ArgumentException(Throwable throwable) {
        super(throwable);
    }

}
