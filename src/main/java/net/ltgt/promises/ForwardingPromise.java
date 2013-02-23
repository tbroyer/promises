package net.ltgt.promises;

public abstract class ForwardingPromise<V> implements Promise<V> {

  protected abstract Promise<V> getDelegate();

  @Override
  public <R> Promise<R> then(ChainingCallback<? super V, R> callback) {
    return getDelegate().then(callback);
  }

  @Override
  public void then(LeafCallback<? super V> callback) {
    getDelegate().then(callback);
  }
}
