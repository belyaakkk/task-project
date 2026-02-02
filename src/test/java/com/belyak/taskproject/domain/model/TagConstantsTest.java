package com.belyak.taskproject.domain.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TagConstantsTest {

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<TagConstants> constructor = TagConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testConstant() {
        assertThat(TagConstants.DEFAULT_COLOR).isEqualTo("#312F2C");
    }
}
