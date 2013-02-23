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

  /**
   * A callback for chaining promises.
   * <p>
   * When a promise is fulfilled or rejected
   *
   * @param <V> type of the fulfilled value
   * @param <R> type of the promised value
   */
  abstract class ChainingCallback<V, R> {
    public abstract Promise<R> onFulfilled(@Nullable V value);

    public Promise<R> onRejected(Throwable reason) {
      return Promises.rejected(reason);
    }
  }

  abstract class LeafCallback<V> {
    public abstract void onFulfilled(@Nullable V value);

    public void onRejected(Throwable reason) {
      if (reason instanceof RuntimeException) {
        throw (RuntimeException) reason;
      }
      if (reason instanceof Error) {
        throw (Error) reason;
      }
      throw new RuntimeException(reason);
    }
  }

  <R> Promise<R> then(ChainingCallback<? super V, R> callback);

  void then(LeafCallback<? super V> callback);
}
