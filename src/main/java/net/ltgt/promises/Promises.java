package net.ltgt.promises;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.ltgt.promises.Promise.LeafCallback;

public final class Promises {

  public static <V> Promise<V> fulfilled(final V value) {
    return new Promise<V>() {
      @Override
      public <R> Promise<R> then(ChainingCallback<? super V, R> callback) {
        try {
          return callback.onFulfilled(value);
        } catch (Throwable t) {
          return rejected(t);
        }
      }
      @Override
      public <R> Promise<R> then(ChainingImmediateCallback<? super V, R> callback) {
        try {
          return fulfilled(callback.onFulfilled(value));
        } catch (Throwable t) {
          return rejected(t);
        }
      }
      @Override
      public void then(LeafCallback<? super V> callback) {
        callback.onFulfilled(value);
      }
    };
  }

  public static <V> Promise<V> rejected(final Throwable reason) {
    return new Promise<V>() {
      @Override
      public <R> Promise<R> then(ChainingCallback<? super V, R> callback) {
        try {
          return callback.onRejected(reason);
        } catch (Throwable t) {
          return rejected(t);
        }
      }
      @Override
      public <R> Promise<R> then(ChainingImmediateCallback<? super V, R> callback) {
        try {
          return fulfilled(callback.onRejected(reason));
        } catch (Throwable t) {
          return rejected(t);
        }
      }
      @Override
      public void then(LeafCallback<? super V> callback) {
        callback.onRejected(reason);
      }
    };
  }

  @SafeVarargs
  public static <V> Promise<List<V>> wait(Promise<? extends V>... promises) {
    return wait(Arrays.asList(promises));
  }

  public static <V> Promise<List<V>> wait(Collection<? extends Promise<? extends V>> promises) {
    if (promises.isEmpty()) {
      return fulfilled(Collections.<V>emptyList());
    }
    final FulfillablePromise<List<V>> result = FulfillablePromise.create();
    final AtomicInteger remaining = new AtomicInteger(promises.size());
    final AtomicBoolean completed = new AtomicBoolean();
    final List<V> values = new ArrayList<>(promises.size());

    // fill with nulls
    for (int i = 0; i < promises.size(); i++) {
      values.add(null);
    }
    assert values.size() == promises.size();

    int i = 0;
    for (Promise<? extends V> promise : promises) {
      final int pos = i++;
      promise.then(new LeafCallback<V>() {
        @Override
        public void onFulfilled(V value) {
          if (completed.get()) {
            return;
          }
          values.set(pos, value);
          if (remaining.decrementAndGet() == 0) {
            result.fulfill(values);
          }
        }

        @Override
        public void onRejected(Throwable reason) {
          if (completed.compareAndSet(false, true)) {
            values.clear();
            result.reject(reason);
          }
        }
      });
    }
    return result;
  }

  private Promises() {}
}
