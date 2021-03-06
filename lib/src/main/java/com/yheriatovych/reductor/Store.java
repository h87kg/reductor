package com.yheriatovych.reductor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * State container which dispatches actions with provided reducer
 * <p>
 * Note: Having immutable state is strongly recommended
 *
 * @param <State> type of state to be stored and manipulated
 */
public class Store<State> {
    private final Reducer<State> reducer;
    private final Middleware.NextDispatcher dispatcher;
    private final List<StateChangeListener<State>> listeners = new CopyOnWriteArrayList<>();
    private volatile State state;

    private Store(Reducer<State> reducer, State state, Middleware<State>[] middlewares) {
        this.reducer = reducer;
        this.state = state;

        Middleware.NextDispatcher nextDispatcher = this::dispatchAction;
        for (int i = middlewares.length - 1; i >= 0; i--) {
            Middleware<State> middleware = middlewares[i];
            final Middleware.NextDispatcher finalNextDispatcher = nextDispatcher;
            nextDispatcher = action -> middleware.dispatch(Store.this, action, finalNextDispatcher);
        }
        this.dispatcher = nextDispatcher;
    }

    private void dispatchAction(final Object actionObject) {
        if (actionObject instanceof Action) {
            final Action action = (Action) actionObject;
            synchronized (this) {
                state = reducer.reduce(state, action);
            }
            for (StateChangeListener<State> listener : listeners) {
                listener.onStateChanged(state);
            }
        } else {
            throw new IllegalArgumentException(String.format("action %s of %s is not instance of %s, use custom Middleware to dispatch another types of actions", actionObject, actionObject.getClass(), Action.class));
        }
    }

    /**
     * Create store with given {@link Reducer}, initalState and possible array of {@link Middleware}
     *
     * @param reducer      Reducer of type S which will be used to dispatch actions
     * @param initialState state to be initial state of create store
     * @param middlewares  array of middlewares to be used to dispatch actions in the same order as provided
     *                     look {@link Middleware} for more information
     * @param <S>          type of state to hold and maintain
     * @return Store initialised with initialState
     */
    @SafeVarargs
    public static <S> Store<S> create(Reducer<S> reducer, S initialState, Middleware<S>... middlewares) {
        return new Store<>(reducer, initialState, middlewares);
    }

    /**
     * @return state, this Store currently holds
     */
    public State getState() {
        return state;
    }

    /**
     * Dispatch action through {@link Reducer} and store the next state
     *
     * @param action action to be dispatched, usually instance of {@link Action}
     *               but custom {@link Middleware} can be used to support other types of actions
     */
    public void dispatch(final Object action) {
        dispatcher.call(action);
    }

    /**
     * Subscribe for state changes
     * <p>
     * Note: current state which Store holds will not be dispatched immediately after subscribe
     *
     * @param listener callback which will be notified each time state changes
     * @return instance of {@link Cancelable} to be used to cancel subscription (remove listener)
     */
    public Cancelable subscribe(final StateChangeListener<State> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    /**
     * Listener which will be notified each time state changes.
     * <p>
     * Look {@link #subscribe(StateChangeListener)}
     */
    public interface StateChangeListener<S> {
        void onStateChanged(S state);
    }
}
