package net.ltgt.promises;

import javax.annotation.Nullable;

/**
 * A promise represents a value that may not be available yet.
 * <p>
 * A promise must be in one of three states: pending, fulfilled, or rejected.
 * <dl>
 * <dt>When in pending, a promise:</dt>
 * <dd>may transition to either the fulfilled or rejected state.</dd>
 * 
 * <dt>When in fulfilled, a promise:</dt>
 * <dd>must not transition to any other state.</dd>
 * <dd>must have a value, which must not change.</dd>
 * 
 * <dt>When in rejected, a promise:</dt>
 * <dd>must not transition to any other state.</dd>
 * <dd>must have a reason, which must not change.</dd>
 * </dl>
 * 
 * @param <V> type of the promised value
 */
public interface Promise<V> {

  abstract class Callback<V, R> {
    public abstract Promise<R> onFulfilled(@Nullable V value);

    public Promise<R> onRejected(Throwable reason) {
      return Promises.rejected(reason);
    }
  }

  abstract class ImmediateCallback<V, R> {
    public abstract R onFulfilled(@Nullable V value) throws Throwable;

    public R onRejected(Throwable reason) throws Throwable {
      throw reason;
    }
  }

  abstract class DoneCallback<V> {
    public abstract void onFulfilled(@Nullable V value);

    public void onRejected(Throwable reason) {
      Promises.propagate(reason);
    }
  }

  <R> Promise<R> then(Callback<? super V, R> callback);

  <R> Promise<R> then(ImmediateCallback<? super V, R> callback);

  void done(DoneCallback<? super V> callback);

  void done();
}
