import { StateHandler } from "../types/types.js";

export function stateHandler<T extends object>(initial: T): StateHandler<T> {
    let state: Readonly<T> = initial;

    function setState(update: Partial<T>): void {
        state = { ...state, ...update };
    }

    function getState(): Readonly<T> {
        return state;
    }

    return {
        getState,
        setState,
    };
}
