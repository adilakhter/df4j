/*
 * Copyright 2011 by Alexei Kaigorodov
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.df4j.core.boundconnector;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * scalar inlet for messages
 *
 * @param <T> the type of the message
 */
public interface Port<T> extends BiConsumer<T, Throwable> {

    /**
     * If this ScalarSubscriber was not already completed, sets it completed state.
     *
     * @param message the completion value
     */
    void post(T message);

    /**
     * successful end of stream
     */
    default void complete() {}

    /**
     * If this ScalarSubscriber was not already completed, sets it completed state.
     *
     * @param ex the completion exception
     */
    default void postFailure(Throwable ex) {}

    static <T> Port<T> fromCompletable(CompletableFuture<T> completable) {
        return new Port<T>() {

            @Override
            public void post(T message) {
                completable.complete(message);
            }

            @Override
            public void postFailure(Throwable ex) {
                completable.completeExceptionally(ex);
            }
        };
    }

    /**
     * to pass data from  {@link CompletableFuture} to ScalarSubscriber using     *
     * <pre>
     *     completableFuture.whenComplete(scalarSubscriber)
     * </pre>
     * @param r
     * @param throwable
     */
    @Override
    default void accept(T r, Throwable throwable) {
        if (throwable != null) {
            postFailure(throwable);
        } else {
            post(r);
        }
    }
}
