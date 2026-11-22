package com.hotelbooking.support;

import com.hotelbooking.common.domain.error.DomainException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class DomainAssertions {

    private DomainAssertions() {
    }

    /** Asserts both the exception category (which decides the HTTP status) and the stable error code. */
    public static void assertRejected(Class<? extends DomainException> type, String code, ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(type)
                .satisfies(ex -> assertThat(((DomainException) ex).code()).isEqualTo(code));
    }
}
