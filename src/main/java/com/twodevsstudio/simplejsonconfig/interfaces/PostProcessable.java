package com.twodevsstudio.simplejsonconfig.interfaces;

import java.lang.reflect.Field;
import lombok.SneakyThrows;

public interface PostProcessable {

    @SneakyThrows
    static void deepPostProcess(Object object) {

        for (Field declaredField : object.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);

            Object fieldValue = declaredField.get(object);
            if (!(fieldValue instanceof PostProcessable)) {
                continue;
            }

            recursivePostProcess((PostProcessable) fieldValue);
        }
    }

    @SneakyThrows
    static void recursivePostProcess(PostProcessable postProcessable) {


        for (Field declaredField : postProcessable.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);

            Object fieldValue = declaredField.get(postProcessable);
            if (!(fieldValue instanceof PostProcessable)) {
                continue;
            }

            recursivePostProcess((PostProcessable) fieldValue);
        }

        postProcessable.gsonPostProcess();
    }

    void gsonPostProcess();

}
