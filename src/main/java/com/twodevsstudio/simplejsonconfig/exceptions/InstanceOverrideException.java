package com.twodevsstudio.simplejsonconfig.exceptions;

public class InstanceOverrideException extends RuntimeException {

    public InstanceOverrideException() {

        super("Your plugin is already registered. Are you sure that you're not trying to register it twice somewhere?");
    }

    public InstanceOverrideException(String message) {

        super(message);
    }

    public InstanceOverrideException(String message, Throwable cause) {

        super(message, cause);
    }

    public InstanceOverrideException(Throwable cause) {

        super(cause);
    }

    public InstanceOverrideException(String message,
                                     Throwable cause,
                                     boolean enableSuppression,
                                     boolean writableStackTrace
    ) {

        super(message, cause, enableSuppression, writableStackTrace);
    }

}
